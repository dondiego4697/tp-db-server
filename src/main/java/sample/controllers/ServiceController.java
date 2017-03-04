package sample.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("/service/")
public class ServiceController {
    public ServiceController(){

    }

    //Очистка всех данных в базе
    @RequestMapping(path = "/clear", method = RequestMethod.POST)
    public void clear(){

    }

    //Получение информации о базе данных
    @RequestMapping(path = "/status", method = RequestMethod.GET)
    public void getStatus(){

    }
}
