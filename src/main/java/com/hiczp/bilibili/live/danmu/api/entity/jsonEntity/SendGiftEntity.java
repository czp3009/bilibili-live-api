package com.hiczp.bilibili.live.danmu.api.entity.jsonEntity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.hiczp.bilibili.live.danmu.api.entity.JSONEntity;

/**
 * Created by czp on 17-5-24.
 */
public class SendGiftEntity extends JSONEntity {
    public SendGiftEntityData data;

    public class SendGiftEntityData {
        public String giftName;
        public Integer num;
        public String uname;
        public Integer rcost;
        public Integer uid;
        public JSONArray top_list;
        public Long timestamp;  //Unix时间戳
        public Integer giftId;
        public Integer giftType;
        public String action;
        //json中是super, 与java关键字冲突
        @JSONField(name = "super")
        public Integer superI;
        public Integer price;
        public String rnd;
        public Integer newMedal;
        public Integer newTitle;
        //medal可能是数字也可能是数组. 为数组时(JSONObject), 包含三个字段 medalId(Integer), medalName(String), level(Integer)
        public Object medal;
        public String title;
        public String beatId;
        public String gold;
        public Integer silver;
        public JSONArray notice_msg;
        public SendGiftEntityDataCapsule capsule;

        public class SendGiftEntityDataCapsule {
            public SendGiftEntityDataCapsuleColorful normal;
            public SendGiftEntityDataCapsuleColorful colorful;

            public class SendGiftEntityDataCapsuleColorful {
                public Integer coin;
                public Integer change;
                public SendGiftEntityDataCapsuleColorfulProgress progress;

                public class SendGiftEntityDataCapsuleColorfulProgress {
                    public Integer now;
                    public Integer max;
                }
            }
        }
    }
}
