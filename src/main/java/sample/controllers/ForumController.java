package sample.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjForum;
import sample.objects.ObjThread;
import sample.objects.ObjUser;
import sample.sql.ForumService;
import sample.sql.UserService;

/**
 * Created by Denis on 17.02.2017.
 */
@RestController
@RequestMapping("/api/forum/")
public class ForumController {

    @Autowired
    ForumService forumService;

    @Autowired
    UserService userService;

    //Создание форума
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity<String> createForum(@RequestBody ObjForum body) {
        //System.out.println("Create FORUM with slug " + body.getSlug());
        return (forumService.createForum(body));
    }

    //Создание ветки
    @RequestMapping(path = "/{slug}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createThread(@RequestBody ObjThread body,
                                               @PathVariable(name = "slug") String slug) {
        //System.out.println("Create THREAD with slug " + slug);
        ObjUser objUser = userService.getObjUser(body.getAuthor());
        ResponseEntity<String> responseEntity = forumService.createThread(body, slug, objUser);
        if(responseEntity.getStatusCode().equals(HttpStatus.CREATED)){
            forumService.incrementThreads(slug);
            try {
                forumService.addInLinkUserForum(slug, objUser.getId());
            } catch (Exception e){
            }
        }
        return responseEntity;
    }

    //Получение информации о форуме
    @RequestMapping(path = "/{slug}/details", method = RequestMethod.GET)
    public ResponseEntity<String> getForumDetails(@PathVariable(name = "slug") String slug) {
        return (forumService.getForumDetails(slug));
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