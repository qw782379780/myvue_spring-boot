package com.qs.redis_dao;

import com.qs.Utils.RedisUtil;
import com.qs.model.OneUploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class VideoDao {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 存储文件，存储文件信息到数据库
     * @param oneUploadFile
     * @param path
     */
    public void add(OneUploadFile oneUploadFile, Path path) {
        oneUploadFile.setFilePath(path.toString());
        String fileString = "video:video_id:";
        String video_count = redisUtil.get("video_count");
        if(video_count==null){
            fileString += "1:episode:1:";
            redisUtil.set("video_count", "1");
            redisUtil.set("video:video_id:1:episode_count", "1");
        }else{
            if(oneUploadFile.getFdId()==null?false:("".equals(oneUploadFile.getFdId()))||("null".equals(oneUploadFile.getFdId()))?false:Integer.valueOf(oneUploadFile.getFdId()) < Integer.valueOf(video_count)) {
                String episode_count = redisUtil.get("video:video_id:" + oneUploadFile.getFdId() + ":episode_count");
                if (episode_count == null) {
                    fileString += String.valueOf(Integer.valueOf(video_count) + 1) + ":episode:1:";
                    redisUtil.set("video:video_id:"+String.valueOf(Integer.valueOf(video_count) + 1)+":episode_count", "1");
                    redisUtil.set("video_count", String.valueOf(Integer.valueOf(video_count) + 1));
                }else{
                    fileString += oneUploadFile.getFdId() + ":episode:"+String.valueOf(Integer.valueOf(episode_count) + 1)+":";
                    redisUtil.set("video:video_id:"+oneUploadFile.getFdId()+":episode_count", String.valueOf(Integer.valueOf(episode_count) + 1));
                }
            }else{
                fileString += String.valueOf(Integer.valueOf(video_count) + 1) + ":episode:1:";
                redisUtil.set("video:video_id:"+String.valueOf(Integer.valueOf(video_count) + 1)+":episode_count", "1");
                redisUtil.set("video_count", String.valueOf(Integer.valueOf(video_count) + 1));
            }
        }
        redisUtil.set(fileString+"title",oneUploadFile.getTitle());
        redisUtil.set(fileString+"tag",oneUploadFile.getTag());
        redisUtil.set(fileString+"source",oneUploadFile.getSource());
        redisUtil.set(fileString+"filepath",oneUploadFile.getFilePath());
    }

    /**
     * 获取视频列表
     * @return
     */
    public List<OneUploadFile> getVideoList() {
        String video_count = redisUtil.get("video_count");
        List<OneUploadFile> list = new ArrayList<OneUploadFile>();
        if (video_count == null) {
            return null;
        } else {
            Integer vc = Integer.valueOf(video_count);
            for (int i = 0; i < vc; i++) {
                Set<String> video1 = redisUtil.getKeys("video:video_id:" + (i + 1) + ":episode:1:*");
                Iterator<String> iterator = video1.iterator();
                OneUploadFile file = new OneUploadFile();
                file.setFdId(i + 1 + "");
                while (iterator.hasNext()) {
                    String field = iterator.next();
                    String[] temp = field.split(":");
                    if ("title".equals(temp[5])) {
                        file.setTitle(redisUtil.get(field));
                    } else if ("tag".equals(temp[5])) {
                        file.setTag(redisUtil.get(field));
                    } else if ("source".equals(temp[5])) {
                        file.setSource(redisUtil.get(field));
                    } else if ("filepath".equals(temp[5])) {
                        file.setFilePath(redisUtil.get(field));
                    } else {
                    }
                }
                if(file.getFilePath()!=null&&!"".equals(file.getFilePath())){
                    list.add(file);
                }
            }
        }
        return list;
    }

    /**
     * 获取视频集
     * @param id
     * @return
     */
    public List<OneUploadFile> getVideoPage(String id) {
        String episode_count = redisUtil.get("video:video_id:" + id + ":episode_count");
        List<OneUploadFile> list = new ArrayList<OneUploadFile>();
        for (int i = 0; i < Integer.valueOf(episode_count); i++) {
            Set<String> video = redisUtil.getKeys("video:video_id:" + id + ":episode:" + (i + 1)+":*");
            Iterator<String> iterator = video.iterator();
            OneUploadFile file = new OneUploadFile();
            while (iterator.hasNext()) {
                String field = iterator.next();
                String[] temp = field.toString().split(":");
                if ("title".equals(temp[5])) {
                    file.setTitle(redisUtil.get(field));
                } else if ("tag".equals(temp[5])) {
                    file.setTag(redisUtil.get(field));
                } else if ("source".equals(temp[5])) {
                    file.setSource(redisUtil.get(field));
                } else if ("filepath".equals(temp[5])) {
                    file.setFilePath(redisUtil.get(field));
                } else {
                }
            }
            file.setFdId(id);
            file.setEpisode(String.valueOf(i + 1));
            list.add(file);
        }
        return list;
    }

    /**
     * 根据id删除一个视频集
     * @param id
     * @return
     */
    public String delete(String id) {
        String video_count = redisUtil.get("video_count");
        Set<String> video = redisUtil.getKeys("video:video_id:"+ id + ":*");
        Iterator<String> iterator = video.iterator();
        while (iterator.hasNext()){
            String field = iterator.next();
            redisUtil.remove(field);
        }
//        redisUtil.set("video_count", String.valueOf(Integer.valueOf(video_count)-1));
        return "删除成功";
    }

    /**
     * 根据ids删除多个视频集
     * @param ids
     * @return
     */
    public String deleteAll(String[] ids) {
        for(String id:ids){
            delete(id);
        }
        return "删除多个video成功";
    }

    /**
     * 根据id和episode删除一集
     * @param id
     * @param episode
     * @return
     */
    public String deleteEpisode(String id, String episode) {
        Set<String> video = redisUtil.getKeys("video:video_id:"+ id + ":episode:" + episode +":*");
        Iterator<String> iterator = video.iterator();
        while (iterator.hasNext()){
            String field = iterator.next();
            redisUtil.remove(field);
        }
        String video_episode_count = redisUtil.get("video:video_id:"+ id + ":episode_count");
        redisUtil.set("video:video_id:"+ id + ":episode_count", String.valueOf(Integer.valueOf(video_episode_count)-1));
        String isExist = redisUtil.get("video:video_id:"+ id + ":*");
        String video_count = redisUtil.get("video_count");
        if(isExist==null||"".equals(isExist)){
//            redisUtil.set("video_count", String.valueOf(Integer.valueOf(video_count)-1));
        }
        return "删除episode成功";
    }

    /**
     * 根据id和episodes删除多集
     * @param id
     * @param episodes
     * @return
     */
    public String deleteEpisodeAll(String id, String[] episodes) {
        String video_episode_count = redisUtil.get("video:video_id:"+ id + ":episode_count");
        int count = 0;
        Set<String> video = null;
        Iterator<String> iterator = null;
        for(String episode:episodes){
            video = redisUtil.getKeys("video:video_id:"+ id + ":episode:" + episode +":*");
            iterator = video.iterator();
            while (iterator.hasNext()){
                String field = iterator.next();
                redisUtil.remove(field);
            }
            count++;
        }
        redisUtil.set("video:video_id:"+ id + ":episode_count", String.valueOf(Integer.valueOf(video_episode_count)-count));
        String isExist = redisUtil.get("video:video_id:"+ id + ":*");
        String video_count = redisUtil.get("video_count");
        if(isExist==null||"".equals(isExist)){
//            redisUtil.set("video_count", String.valueOf(Integer.valueOf(video_count)-1));
        }
        return "删除多个episode成功";
    }

}
