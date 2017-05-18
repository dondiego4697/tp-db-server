package sample.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjPost;
import sample.objects.ObjThread;
import sample.objects.ObjVote;
import sample.sql.ThreadService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("api/thread/")
public class ThreadController {

    private final ThreadService threadService;

    public ThreadController(JdbcTemplate jdbcTemplate) {
        this.threadService = new ThreadService(jdbcTemplate);
    }

    //Создание новых постов
    @RequestMapping(path = "/{slug_or_id}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createPost(@PathVariable(name = "slug_or_id") String slug_or_id,
                                             @RequestBody ArrayList<ObjPost> body) {
        System.out.println("Create POST with slug/id " + slug_or_id);
        return (threadService.createPosts(body, slug_or_id));
    }

    //Получение информации о ветке обсуждения
    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.GET)
    public ResponseEntity<String> getThreadDetails(@PathVariable(name = "slug_or_id") String slug_or_id) {
        return (threadService.getThreadDetails(slug_or_id));
    }

    //Обновление ветки
    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.POST)
    public ResponseEntity<String> updateThread(@PathVariable(name = "slug_or_id") String slug_or_id,
                             @RequestBody ObjThread body) {
        return (threadService.updateThread(body, slug_or_id));
    }

    //Сообщения данной ветви обсуждения
    @RequestMapping(path = "/{slug_or_id}/posts", method = RequestMethod.GET)
    public ResponseEntity<String> getThreadPosts(@PathVariable(name = "slug_or_id") String slug_or_id,
                               @RequestParam(value = "limit", required = false) Integer limit,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "desc", required = false) boolean desc,
                               @RequestParam(value = "marker", required = false, defaultValue = "0") Integer marker) {
        return (threadService.getThreadPosts(slug_or_id, limit, sort, desc, marker));
    }

    //Проголосовать за ветвь обсуждения
    @RequestMapping(path = "/{slug_or_id}/vote", method = RequestMethod.POST)
    public ResponseEntity<String> voteThread(@PathVariable(name = "slug_or_id") String slug_or_id,
                                             @RequestBody ObjVote body) {
        System.out.println("Create VOTE with slug/id " + slug_or_id);
        return (threadService.vote(body, slug_or_id));
    }
}
