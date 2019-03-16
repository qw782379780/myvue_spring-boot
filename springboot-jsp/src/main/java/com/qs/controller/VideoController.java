package com.qs.controller;

import com.qs.model.OneUploadFile;
import com.qs.redis_dao.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/videoController")
@CrossOrigin
public class VideoController {
    @Value("./webapps/myVideoWeb/static/video/")
    private String UPLOAD_FOLDER;

    @Autowired
    private VideoDao videoDao;

    @PostMapping("/add")
    public Object singleFileUpload(MultipartFile file, OneUploadFile oneUploadFile) {
        if (Objects.isNull(file) || file.isEmpty()) {
            return "文件为空，请重新上传";
        }
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_FOLDER + file.getOriginalFilename());
            //如果没有files文件夹，则创建
            if (!Files.isWritable(path)) {
                Files.createDirectories(Paths.get(UPLOAD_FOLDER));
            }
            //文件写入指定路径
            Files.write(path, bytes);
            //保存文件信息到redis
            videoDao.add(oneUploadFile, path);
            return "文件上传成功";
        } catch (IOException e) {
            e.printStackTrace();
            return "后端异常...";
        }
    }

    /**
     * 获取视频列表
     * @return
     */
    @RequestMapping("/videoList")
    public List<OneUploadFile> getVideoList() {
        return videoDao.getVideoList();
    }

    /**
     * 根据id获取一个视频集
     * @return
     */
    @RequestMapping("/videoPage")
    public List<OneUploadFile> getVideoPage(String id) {
        return videoDao.getVideoPage(id);
    }

    /**
     * 根据id删除一个视频集
     * @return
     */
    @RequestMapping("/delete")
    public String delete(String id) {
        return videoDao.delete(id);
    }

    /**
     * 根据id删除多个视频集
     * @return
     */
    @RequestMapping("/deleteAll")
    public String deleteAll(String[] ids) {
        return videoDao.deleteAll(ids);
    }

    /**
     * 根据id和episode删除某一集
     * @return
     */
    @RequestMapping("/deleteEpisode")
    public String deleteEpisode(String id, String episode) {
        return videoDao.deleteEpisode(id, episode);
    }

    /**
     * 根据id和episode删除多集
     * @return
     */
    @RequestMapping("/deleteEpisodeAll")
    public String deleteEpisodeAll(String id, String[] episodes) {
        return videoDao.deleteEpisodeAll(id, episodes);
    }
}