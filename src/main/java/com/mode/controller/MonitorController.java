package com.mode.controller;

import com.mode.model.SampleRepository;
import com.mode.presenter.IndexPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitorController {

    @Autowired
    SampleRepository sampleRepository;

    @GetMapping("/")
    public String getIndex(Model thModel){
        thModel.addAttribute("presenter", new IndexPresenter(sampleRepository.findAll()));
        return "index";
    }
}
