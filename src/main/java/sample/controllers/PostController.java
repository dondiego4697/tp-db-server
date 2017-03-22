package sample.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjPost;
import sample.objects.ObjThread;
import sample.sql.PostService;
import sample.sql.ThreadService;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("api/post/")
public class PostController {

    private final PostService postService;

    public PostController(JdbcTemplate jdbcTemplate) {
        this.postService = new PostService(jdbcTemplate);
    }

    //Получение информации о ветке
    @RequestMapping(path = "/{id}/details", method = RequestMethod.GET)
    public ResponseEntity<String> getPostDetail(@PathVariable(name = "id") Integer id,
                                                @RequestParam(value = "related", defaultValue = "") String related) {
        return (postService.get(id, related));
    }

    //Изменение сообщения
    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST)
    public ResponseEntity<String> changePostDetail(@PathVariable(name = "id") Integer id,
                                 @RequestBody ObjPost body) {
        return (postService.update(id, body));
    }
}
