package com.example.sweater;

import com.example.sweater.domain.Message;
import com.example.sweater.repos.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GreetingController {

    @Autowired
    private MessageRepo messageRepo;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping
    public String main(Model model) {
        Iterable<Message> messages = messageRepo.findAll();
        model.addAttribute("message", messages);
        return "main";
    }

    @PostMapping
    public String sendMessage(@RequestParam String text, @RequestParam String tag, Model model) {
        Message message = new Message(text, tag);
        messageRepo.save(message);
        Iterable<Message> messages = messageRepo.findAll();
        model.addAttribute("message", messages);
        return "main";
    }

    @PostMapping("filter")
    public String filter(@RequestParam String tag, Model model) {
        Iterable<Message> messages;

        if(tag != null && !tag.isEmpty()) {
            messages = messageRepo.findByTag(tag);
        } else
            messages = messageRepo.findAll();

        model.addAttribute("message", messages);
        return "main";
    }

}
