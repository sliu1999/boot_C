package sliu.web.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sliu.domain.QueueInfo;
import sliu.service.QueueInfoService;


@RestController
@RequestMapping("/api")
public class TestResource {

    @Autowired
    private QueueInfoService queueInfoService;

    @GetMapping(value = "/test")
    public QueueInfo createDbLog() {
        try {
            return queueInfoService.selectQueueByName("2");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}