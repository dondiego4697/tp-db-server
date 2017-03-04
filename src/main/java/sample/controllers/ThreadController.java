package sample.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("/thread/")
public class ThreadController {
    public ThreadController(){

    }

    //Создание новых постов
    @RequestMapping(path = "/{slug_or_id}/create", method = RequestMethod.POST)
    public void createPost(@PathVariable(name = "slug_or_id") String slug,
                           @PathVariable(name = "slug_or_id") Integer id){

    }

    //Получение информации о ветке обсуждения
    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.GET)
    public void getThreadDetails(@PathVariable(name = "slug_or_id") String slug,
                                 @PathVariable(name = "slug_or_id") Integer id){

    }

    //Обновление ветки
    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.POST)
    public void updateThread(@PathVariable(name = "slug_or_id") String slug,
                             @PathVariable(name = "slug_or_id") Integer id){

    }

    //Сообщения данной ветви обсуждения
    @RequestMapping(path = "/{slug_or_id}/posts", method = RequestMethod.GET)
    public void getThreadPosts(@PathVariable(name = "slug_or_id") String slug,
                               @PathVariable(name = "slug_or_id") Integer id){

    }

    //Проголосовать за ветвь обсуждения
    @RequestMapping(path = "/{slug_or_id}/vote", method = RequestMethod.POST)
    public void voteThread(@PathVariable(name = "slug_or_id") String slug,
                           @PathVariable(name = "slug_or_id") Integer id){

    }
}
