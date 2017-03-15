package sample.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjForum;
import sample.sql.ForumService;

import javax.servlet.http.HttpServletResponse;

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
    public ResponseEntity<String> createForum(@RequestBody ObjForum body) {
        return (new ForumService().create(body));
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