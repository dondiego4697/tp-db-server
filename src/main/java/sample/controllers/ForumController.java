package sample.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Denis on 17.02.2017.
 */
@RestController
@RequestMapping("/forum/")
public class ForumController {
    public ForumController() {

    }

    //Создание форума
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public void createForum() {

    }

    //Создание ветки
    @RequestMapping(path = "/{slug}/create", method = RequestMethod.POST)
    public void createThread(@PathVariable(name = "slug") String slug) {

    }

    //Получение информации о форуме
    @RequestMapping(path = "/{slug}/details", method = RequestMethod.GET)
    public void getForumDetails(@PathVariable(name = "slug") String slug) {

    }

    //Список ветвей обсуждения форума
    @RequestMapping(path = "/{slug}/threads", method = RequestMethod.GET)
    public void getForumThreads(@PathVariable(name = "slug") String slug) {

    }

    //Пользователи данного форума
    @RequestMapping(path = "/{slug}/users", method = RequestMethod.GET)
    public void getForumUsers(@PathVariable(name = "slug") String slug) {

    }
}