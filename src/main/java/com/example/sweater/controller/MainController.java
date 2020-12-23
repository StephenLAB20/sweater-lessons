package com.example.sweater.controller;

import com.example.sweater.domain.Message;
import com.example.sweater.domain.User;
import com.example.sweater.repos.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Controller
public class MainController {

    @Autowired
    private MessageRepo messageRepo;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String greeting() {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String tag, Model model) {
        Iterable<Message> messages = messageRepo.findAll();

        if(tag != null && !tag.isEmpty()) {
            messages = messageRepo.findByTag(tag);
        } else
            messages = messageRepo.findAll();

        model.addAttribute("messages", messages);
        model.addAttribute("tag", tag);
        return "main";
    }

    @PostMapping("/main")
    public String sendMessage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user,
            @RequestParam(required = true) String text,
            @RequestParam String tag, Model model) throws IOException {
        Message message = new Message(text, tag, user);
        System.out.println("message " + message.getText());

        if (message.getText() != null && !message.getText().isEmpty()) {
            saveFile(file, message);

            messageRepo.save(message);
            Iterable<Message> messages = messageRepo.findAll();
            model.addAttribute("messages", messages);
        }

        return "redirect:/main";
    }

    private void saveFile(MultipartFile file, Message message) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + resultFileName));

            message.setFilename(resultFileName);
        }
    }

    @GetMapping("/user-messages/{id}")
    public String userMessages(@AuthenticationPrincipal User currentUser,
                               @PathVariable("id") User user,
                               @RequestParam(required = false) Message message,
                               Model model) {

        Set<Message> messages = user.getMessages();
        model.addAttribute("userChannel", user);
        model.addAttribute("isCurrentUser", currentUser.equals(user));
        model.addAttribute("isSubscriber", user.getSubscribers().contains(currentUser));
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("messages", messages);
        model.addAttribute("message", message);

        model.addAttribute("user", user);
//        System.out.println("currentUser.getId() " + currentUser.getId());

//        model.addAttribute("isCurrentUser", currentUser.equals(user));

        return "userMessages";
    }

    @PostMapping("/user-messages/{userId}")
    public String updateMessage(@AuthenticationPrincipal User currentUser,
                                @PathVariable("userId") Long user,
                                @RequestParam("id") Message message,
                                @RequestParam("text") String text,
                                @RequestParam("tag") String tag,
                                @RequestParam("file") MultipartFile file) throws IOException {


        if(message.getAuthor().equals(currentUser)) {
            if(!StringUtils.isEmpty(text)) {
                message.setText(text);
            }
            if(!StringUtils.isEmpty(tag)) {
                message.setTag(tag);
            }

            saveFile(file, message);
            messageRepo.save(message);
        }

        return "redirect:/user-messages/" + user;
    }

}
