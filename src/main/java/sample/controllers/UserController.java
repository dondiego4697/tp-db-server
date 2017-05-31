package sample.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjUser;
import sample.rowsmap.UserMapper;
import sample.sql.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("/api/user/")
public class UserController {

    private final UserService userService;

    UserController(JdbcTemplate jdbcTemplate) {
        this.userService = new UserService(jdbcTemplate);
    }

    //Создание нового пользователя
    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createUser(@RequestBody ObjUser body, @PathVariable(name = "nickname") String nickname) {
        return (userService.create(body, nickname));
    }

    //Получение информации о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.GET)
    public ResponseEntity<String> getUser(@PathVariable(name = "nickname") String nickname) {
        return (userService.get(nickname));
    }

    //Изменение данных о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.POST)
    public ResponseEntity<String> updateUser(@RequestBody ObjUser body,
                                             @PathVariable(name = "nickname") String nickname) {
        return (userService.update(body, nickname));
    }

    /*@Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping(path = "/filltestarea/{name}", method = RequestMethod.POST)
    public ResponseEntity<String> fillTestarea(@PathVariable(name = "name") String name) {

        final List<Object[]> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            final List<Object> result = new ArrayList<>();
            result.add(name + i);
            list.add(result.toArray());
        }
        jdbcTemplate.batchUpdate("INSERT INTO \"user\" (name) VALUES (?)", list);

        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(path = "/fillfriends/{count}", method = RequestMethod.POST)
    public ResponseEntity<String> fillFriends(@PathVariable(name = "count") Integer count) {

        final List<Object[]> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            final List<Object> result = new ArrayList<>();

            int id1 = 0;
            int id2 = 0;
            while (id1 == id2) {
                id1 = new Random().nextInt(count);
                id2 = new Random().nextInt(count);
                final Integer count1 = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM friend WHERE id1=(?) AND id2=(?) OR id2=(?) AND id1=(?)",
                        new Object[]{id1, id2, id1, id2}, Integer.class
                );
                if (count1 > 0 || id1 == 0 || id2 == 0) {
                    id1 = 0;
                    id2 = 0;
                }
                System.out.println("count1=" + count1 + "; id1=" + id1 + "; id2=" + id2);
            }

            result.add(id1);
            result.add(id2);
            list.add(result.toArray());
        }
        jdbcTemplate.batchUpdate("INSERT INTO friend (id1, id2) VALUES (?, ?)", list);

        return new ResponseEntity<>("ok", HttpStatus.OK);
    }*/
}
