package sample.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("/user/")
public class UserController {
    public UserController(){

    }

    //Создание нового пользователя
    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST)
    public void createUser(@PathVariable(name = "nickname") String nickname){

    }

    //Получение информации о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.GET)
    public void getUser(@PathVariable(name = "nickname") String nickname){

    }

    //Изменение данных о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.POST)
    public void updateUser(@PathVariable(name = "nickname") String nickname){

    }
}
