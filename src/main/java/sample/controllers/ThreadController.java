package sample.controllers;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjPost;
import sample.objects.ObjThread;
import sample.objects.ObjVote;
import sample.sql.ThreadService;
import sample.support.ObjSlugOrId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("api/thread/")
public class ThreadController {

    @Autowired
    ThreadService threadService;

    //Создание новых постов
    @RequestMapping(path = "/{slug_or_id}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createPost(@PathVariable(name = "slug_or_id") String slug_or_id,
                                             @RequestBody ArrayList<ObjPost> body) {
        //System.out.println("Create POST with slug/id " + slug_or_id);

        final ObjThread objThread;

        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        if (!objSlugOrId.getFlag()) {
            try {
                objThread = threadService.getObjThreadById(objSlugOrId.getId());
                if (objThread == null) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        } else {
            try {
                objThread = threadService.getObjThreadBySlug(objSlugOrId.getSlug());
                if (objThread == null) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        }
        ResponseEntity<ArrayList<ObjPost>> responseEntity = threadService.createPosts(body, objThread);

        if(responseEntity.getStatusCode().equals(HttpStatus.CONFLICT) || responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)){
            return new ResponseEntity<>("", responseEntity.getStatusCode());
        }

        ArrayList<ObjPost> arr = responseEntity.getBody();
        if(responseEntity.getStatusCode().equals(HttpStatus.CREATED)){
            //System.out.println("IM IN CREATED POSTS");
            threadService.incrementPosts(objThread.getForum(), body.size());

            threadService.addInLinkUserForum(objThread.getForum(),
                    arr.stream()
                            .map(ObjPost::getUserid)
                            .distinct()
                            .collect(Collectors.toList()), 40);
        }

        final JSONArray result = new JSONArray();
        IntStream.range(0, arr.size()).boxed()
                .forEach(i -> {
                    result.put(arr.get(i).getJson());
                });
        return new ResponseEntity<>(result.toString(), HttpStatus.CREATED);
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
        //System.out.println("Create VOTE with slug/id " + slug_or_id);
        return (threadService.vote(body, slug_or_id));
    }
}
