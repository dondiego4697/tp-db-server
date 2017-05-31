package sample.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sample.sql.DBService;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("api/service/")
public class DBController {

    @Autowired
    DBService dbService;

    //Очистка всех данных в базе
    @RequestMapping(path = "/clear", method = RequestMethod.POST)
    public ResponseEntity<String> clear() {
        return (dbService.clear());
    }

    //Получение информации о базе данных
    @RequestMapping(path = "/status", method = RequestMethod.GET)
    public ResponseEntity<String> getStatus() {
        return (dbService.getInfo());
    }
}
