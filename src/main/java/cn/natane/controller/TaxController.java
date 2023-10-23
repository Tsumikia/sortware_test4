package cn.natane.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class TaxController {

    @GetMapping("/tax")
    public String getTax(@RequestParam Double salary) {
        System.out.println(salary);
        double taxRate;
        if (salary <= 5000) {
            taxRate = 0.0;
        } else if (salary <= 8000) {
            taxRate = 0.03;
        } else if (salary <= 17000) {
            taxRate = 0.1;
        } else if (salary <= 30000) {
            taxRate = 0.2;
        } else if (salary <= 40000) {
            taxRate = 0.25;
        } else if (salary <= 60000) {
            taxRate = 0.3;
        } else if (salary <= 85000) {
            taxRate = 0.35;
        } else {
            taxRate = 0.45;
        }
        return String.valueOf(salary * taxRate);
    }

    @GetMapping("haruka")
    public String s() {
        return "haruka";
    }

}
