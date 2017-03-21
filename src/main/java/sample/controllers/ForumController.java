package sample.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjForum;
import sample.objects.ObjThread;
import sample.sql.ForumService;
import sample.sql.UserService;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Denis on 17.02.2017.
 */
@RestController
@RequestMapping("/api/forum/")
public class ForumController {

    private final ForumService forumService;

    public ForumController(JdbcTemplate jdbcTemplate) {
        this.forumService = new ForumService(jdbcTemplate);
    }

    //Создание форума
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity<String> create(@RequestBody ObjForum body) {
        return (forumService.create(body));
    }

    //Создание ветки
    @RequestMapping(path = "/{slug}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createThread(@RequestBody ObjThread body,
                                               @PathVariable(name = "slug") String slug) {
        return (forumService.createThread(body, slug));
    }

    //Получение информации о форуме
    @RequestMapping(path = "/{slug}/details", method = RequestMethod.GET)
    public ResponseEntity<String> getForumDetails(@PathVariable(name = "slug") String slug) {
        return (forumService.details(slug));
    }

    //Список ветвей обсуждения форума
    @RequestMapping(path = "/{slug}/threads", method = RequestMethod.GET)
    public ResponseEntity<String> getForumThreads(@PathVariable String slug,
                                                  @RequestParam(value = "limit", required = false) Integer limit,
                                                  @RequestParam(value = "since", required = false) String since,
                                                  @RequestParam(value = "desc", required = false) Boolean desc) {
        return (forumService.getThreads(slug, limit, since, desc));
    }

    //Пользователи данного форума
    @RequestMapping(path = "/{slug}/users", method = RequestMethod.GET)
    public ResponseEntity<String> getForumUsers(@PathVariable(name = "slug") String slug,
                                                @RequestParam(value = "limit", required = false) Integer limit,
                                                @RequestParam(value = "since", required = false) String since,
                                                @RequestParam(value = "desc", required = false) Boolean desc){
            return (forumService.getForumUsers(slug, limit, since, desc));
    }
}