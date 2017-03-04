package sample.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("/post/")
public class PostController {

    public PostController(){

    }

    //Получение информации о сообщении
    @RequestMapping(path = "/{id}/details", method = RequestMethod.GET)
    public void getPostDetail(@PathVariable(name = "id") Integer id){

    }

    //Изменение сообщения
    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST)
    public void changePostDetail(@PathVariable(name = "id") Integer id){

    }
}
