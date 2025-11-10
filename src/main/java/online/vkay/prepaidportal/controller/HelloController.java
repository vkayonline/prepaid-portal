package online.vkay.prepaidportal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class HelloController {

    @GetMapping("/hello")
    public String helloPublic() {
        return "Hello, World! (Public)";
    }

    @GetMapping("/admin/hello")
    public String helloAdmin() {
        return "Hello, System Admin!";
    }
}

