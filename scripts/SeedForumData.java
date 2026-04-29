import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SeedForumData {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/college_freshman_helper?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345678";
    private static final long ID_BASE = 2050000000000000000L;

    private final Random random = new Random(20260429L);
    private long nextId = ID_BASE;
    private final List<String> tags = List.of("宿舍", "学习", "食堂", "军训", "其他");
    private final List<PostSeed> postSeeds = buildPostSeeds();

    public static void main(String[] args) throws Exception {
        new SeedForumData().run();
    }

    private void run() throws Exception {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            connection.setAutoCommit(false);
            try {
                List<UserRow> users = loadActiveUsers(connection);
                if (users.size() < 6) {
                    throw new IllegalStateException("活跃用户数量过少，当前不足以生成真实互动数据");
                }

                List<UserRow> postingUsers = users.stream().filter(user -> user.role < 8).toList();
                if (postingUsers.size() < 6) {
                    throw new IllegalStateException("普通用户数量过少，当前不足以生成真实帖子");
                }

                List<Long> insertedPostIds = new ArrayList<>();
                List<Long> insertedReplyIds = new ArrayList<>();
                List<Long> insertedLikeIds = new ArrayList<>();

                for (int i = 0; i < postSeeds.size(); i++) {
                    PostSeed seed = postSeeds.get(i);
                    UserRow author = postingUsers.get(i % postingUsers.size());
                    long postId = insertPost(connection, author.id, seed, i);
                    insertedPostIds.add(postId);

                    List<ReplyRow> replies = insertReplies(connection, postId, author, postingUsers, seed, i);
                    for (ReplyRow reply : replies) {
                        insertedReplyIds.add(reply.id);
                    }

                    insertedLikeIds.addAll(insertLikesForPost(connection, postId, author.id, postingUsers, i));
                    for (ReplyRow reply : replies) {
                        insertedLikeIds.addAll(insertLikesForReply(connection, reply, postingUsers, i));
                    }

                    updatePostCounters(connection, postId);
                }

                refreshUserStats(connection);
                connection.commit();

                System.out.printf("seeded posts=%d replies=%d likes=%d%n",
                        insertedPostIds.size(), insertedReplyIds.size(), insertedLikeIds.size());
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private List<UserRow> loadActiveUsers(Connection connection) throws Exception {
        List<UserRow> users = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT id, username, nickname, role FROM sys_user WHERE deleted = 0 AND status = 1 ORDER BY created_at ASC")) {
            while (rs.next()) {
                users.add(new UserRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("nickname"),
                        rs.getInt("role")
                ));
            }
        }
        return users;
    }

    private long insertPost(Connection connection, long userId, PostSeed seed, int index) throws Exception {
        long postId = nextId();
        LocalDateTime createdAt = LocalDateTime.now()
                .minusDays(24L - (index % 24))
                .minusHours((index * 3L) % 18L)
                .minusMinutes((index * 11L) % 50L)
                .truncatedTo(ChronoUnit.MINUTES);

        int viewCount = 1 + (index % 4);
        int collectCount = 1 + (index % 4);
        String sql = "INSERT INTO forum_post (" +
                "id, user_id, title, content, content_type, first_image_url, image_count, tags, view_count, reply_count, like_count, collect_count, status, visibility, audit_reason, operator_admin_id, manual_delete_reason, published_at, last_reply_at, created_at, updated_at, deleted" +
                ") VALUES (" +
                postId + ", " +
                userId + ", " +
                sqlQuote(seed.title) + ", " +
                sqlQuote(seed.htmlContent()) + ", " +
                "1, NULL, 0, " +
                sqlQuote(seed.tag) + ", " +
                viewCount + ", 0, 0, " +
                collectCount + ", 1, 1, NULL, NULL, NULL, " +
                sqlTimestamp(createdAt) + ", NULL, " +
                sqlTimestamp(createdAt) + ", " +
                sqlTimestamp(createdAt) + ", 0)";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
        return postId;
    }

    private List<ReplyRow> insertReplies(Connection connection, long postId, UserRow author, List<UserRow> users, PostSeed seed, int postIndex) throws Exception {
        int rootReplyCount = 1 + random.nextInt(3);
        List<ReplyRow> inserted = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now()
                .minusDays(20L - (postIndex % 20))
                .minusHours(postIndex % 12)
                .truncatedTo(ChronoUnit.MINUTES);

        for (int i = 0; i < rootReplyCount; i++) {
            UserRow replier = pickDifferentUser(users, author.id, postIndex + i);
            String content = buildRootReply(seed, i);
            long replyId = insertReply(connection, postId, replier.id, 0L, null, null, content, baseTime.plusHours(i + 1L));
            inserted.add(new ReplyRow(replyId, postId, replier.id, 0L));

            int childCount = random.nextInt(3);
            for (int j = 0; j < childCount; j++) {
                UserRow childUser = pickDifferentUser(users, replier.id, postIndex + i + j + 7);
                String childContent = buildChildReply(seed, j);
                long childReplyId = insertReply(connection, postId, childUser.id, replyId, replyId, replier.id, childContent, baseTime.plusHours(i + 1L).plusMinutes((j + 1L) * 12L));
                inserted.add(new ReplyRow(childReplyId, postId, childUser.id, replyId));
            }
        }
        return inserted;
    }

    private long insertReply(Connection connection, long postId, long userId, Long parentId, Long replyToReplyId, Long replyToUserId, String content, LocalDateTime createdAt) throws Exception {
        long replyId = nextId();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO forum_reply (" +
                        "id, post_id, user_id, parent_id, reply_to_reply_id, reply_to_user_id, content, content_type, image_url, like_count, child_count, status, visibility, qa_sync_status, operator_admin_id, manual_delete_reason, created_at, updated_at, deleted" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, 1, NULL, 0, 0, 1, 1, 0, NULL, NULL, ?, ?, 0)")) {
            ps.setLong(1, replyId);
            ps.setLong(2, postId);
            ps.setLong(3, userId);
            ps.setLong(4, parentId == null ? 0L : parentId);
            if (replyToReplyId == null) {
                ps.setNull(5, java.sql.Types.BIGINT);
            } else {
                ps.setLong(5, replyToReplyId);
            }
            if (replyToUserId == null) {
                ps.setNull(6, java.sql.Types.BIGINT);
            } else {
                ps.setLong(6, replyToUserId);
            }
            ps.setString(7, content);
            ps.setTimestamp(8, Timestamp.valueOf(createdAt));
            ps.setTimestamp(9, Timestamp.valueOf(createdAt));
            ps.executeUpdate();
        }
        return replyId;
    }

    private List<Long> insertLikesForPost(Connection connection, long postId, long authorId, List<UserRow> users, int postIndex) throws Exception {
        int likeCount = 2 + random.nextInt(6);
        List<UserRow> likers = shuffledUsers(users, postIndex * 17 + 3);
        List<Long> insertedIds = new ArrayList<>();
        for (UserRow liker : likers) {
            if (liker.id == authorId) {
                continue;
            }
            insertedIds.add(insertLikeRecord(connection, liker.id, 1, postId, LocalDateTime.now().minusDays(postIndex % 10L).minusMinutes(random.nextInt(180))));
            if (insertedIds.size() >= likeCount) {
                break;
            }
        }
        return insertedIds;
    }

    private List<Long> insertLikesForReply(Connection connection, ReplyRow reply, List<UserRow> users, int salt) throws Exception {
        int maxLikes = reply.parentId == 0L ? 4 : 3;
        int likeCount = random.nextInt(maxLikes + 1);
        List<UserRow> likers = shuffledUsers(users, salt * 31 + (int) (reply.id % 97));
        List<Long> insertedIds = new ArrayList<>();
        for (UserRow liker : likers) {
            if (liker.id == reply.userId) {
                continue;
            }
            insertedIds.add(insertLikeRecord(connection, liker.id, 2, reply.id, LocalDateTime.now().minusHours(random.nextInt(120)).minusMinutes(random.nextInt(45))));
            if (insertedIds.size() >= likeCount) {
                break;
            }
        }
        return insertedIds;
    }

    private long insertLikeRecord(Connection connection, long userId, int targetType, long targetId, LocalDateTime likedAt) throws Exception {
        long likeId = nextId();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO forum_like_record (" +
                        "id, user_id, target_type, target_id, like_status, liked_at, unliked_at, created_at, updated_at, deleted" +
                        ") VALUES (?, ?, ?, ?, 1, ?, NULL, ?, ?, 0)")) {
            ps.setLong(1, likeId);
            ps.setLong(2, userId);
            ps.setInt(3, targetType);
            ps.setLong(4, targetId);
            ps.setTimestamp(5, Timestamp.valueOf(likedAt));
            ps.setTimestamp(6, Timestamp.valueOf(likedAt));
            ps.setTimestamp(7, Timestamp.valueOf(likedAt));
            ps.executeUpdate();
        }
        return likeId;
    }

    private void updatePostCounters(Connection connection, long postId) throws Exception {
        int replyCount = countByQuery(connection,
                "SELECT COUNT(*) FROM forum_reply WHERE post_id = " + postId + " AND deleted = 0");
        int likeCount = countByQuery(connection,
                "SELECT COUNT(*) FROM forum_like_record WHERE target_type = 1 AND target_id = " + postId + " AND like_status = 1 AND deleted = 0");
        Timestamp lastReplyAt = queryTimestamp(connection,
                "SELECT MAX(created_at) FROM forum_reply WHERE post_id = " + postId + " AND deleted = 0");

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE forum_post SET reply_count = ?, like_count = ?, last_reply_at = ?, view_count = ? WHERE id = ?")) {
            ps.setInt(1, replyCount);
            ps.setInt(2, likeCount);
            if (lastReplyAt == null) {
                ps.setNull(3, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, lastReplyAt);
            }
            ps.setInt(4, 18 + random.nextInt(160));
            ps.setLong(5, postId);
            ps.executeUpdate();
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id FROM forum_reply WHERE post_id = " + postId + " AND deleted = 0")) {
            while (rs.next()) {
                long replyId = rs.getLong(1);
                int childCount = countByQuery(connection,
                        "SELECT COUNT(*) FROM forum_reply WHERE parent_id = " + replyId + " AND deleted = 0");
                int replyLikeCount = countByQuery(connection,
                        "SELECT COUNT(*) FROM forum_like_record WHERE target_type = 2 AND target_id = " + replyId + " AND like_status = 1 AND deleted = 0");
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE forum_reply SET child_count = ?, like_count = ? WHERE id = ?")) {
                    ps.setInt(1, childCount);
                    ps.setInt(2, replyLikeCount);
                    ps.setLong(3, replyId);
                    ps.executeUpdate();
                }
            }
        }
    }

    private void refreshUserStats(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE user_stats us " +
                            "LEFT JOIN (" +
                            "  SELECT user_id, COUNT(*) AS cnt FROM forum_post WHERE deleted = 0 GROUP BY user_id" +
                            ") p ON p.user_id = us.user_id " +
                            "SET us.post_count = COALESCE(p.cnt, 0)");

            statement.executeUpdate(
                    "UPDATE user_stats us " +
                            "LEFT JOIN (" +
                            "  SELECT user_id, COUNT(*) AS cnt FROM forum_reply WHERE deleted = 0 GROUP BY user_id" +
                            ") r ON r.user_id = us.user_id " +
                            "SET us.reply_count = COALESCE(r.cnt, 0)");

            statement.executeUpdate(
                    "UPDATE user_stats us " +
                            "LEFT JOIN (" +
                            "  SELECT user_id, COALESCE(SUM(like_count), 0) AS cnt FROM forum_post WHERE deleted = 0 GROUP BY user_id" +
                            ") p ON p.user_id = us.user_id " +
                            "SET us.post_like_received_count = COALESCE(p.cnt, 0)");

            statement.executeUpdate(
                    "UPDATE user_stats us " +
                            "LEFT JOIN (" +
                            "  SELECT user_id, COALESCE(SUM(like_count), 0) AS cnt FROM forum_reply WHERE deleted = 0 GROUP BY user_id" +
                            ") r ON r.user_id = us.user_id " +
                            "SET us.reply_like_received_count = COALESCE(r.cnt, 0), us.last_calculated_at = NOW()");
        }
    }

    private UserRow pickDifferentUser(List<UserRow> users, long excludedUserId, int salt) {
        List<UserRow> shuffled = shuffledUsers(users, salt);
        for (UserRow user : shuffled) {
            if (user.id != excludedUserId && user.role < 8) {
                return user;
            }
        }
        return shuffled.get(0);
    }

    private List<UserRow> shuffledUsers(List<UserRow> users, int salt) {
        List<UserRow> copy = new ArrayList<>(users);
        Collections.rotate(copy, salt % Math.max(copy.size(), 1));
        return copy;
    }

    private String buildRootReply(PostSeed seed, int index) {
        List<String> variants = List.of(
                "我去年刚经历过这个，先把时间线安排好会轻松很多。建议你优先确认" + seed.tag + "这块的通知，再按清单准备。",
                "这个问题问得挺实际的，我们宿舍当时也讨论过。我的建议是先按学校官方要求来，再根据现场情况补。",
                "可以的，我补一个细节：别只看群消息，最好同时关注学院通知和辅导员发的安排。"
        );
        return variants.get(index % variants.size());
    }

    private String buildChildReply(PostSeed seed, int index) {
        List<String> variants = List.of(
                "同意，我当时就是现场临时改方案，提前多准备一点会更稳。",
                "补充一下，如果是" + seed.tag + "相关的事情，早点去通常选择会更多一些。",
                "我们那届基本也是这样处理的，别太紧张，按步骤来就行。"
        );
        return variants.get(index % variants.size());
    }

    private List<PostSeed> buildPostSeeds() {
        List<PostSeed> list = new ArrayList<>();
        list.add(new PostSeed("宿舍", "新生宿舍床垫要不要自己再加一层？", List.of("学校统一发的床垫看起来不算太厚。", "想问一下大家有没有自己再铺乳胶垫或者褥子，住起来会不会舒服很多？", "如果要带的话，尺寸大概选多少比较合适？")));
        list.add(new PostSeed("宿舍", "宿舍晚上几点会统一熄灯，插排能一直用吗？", List.of("怕开学后作息不适应。", "想提前了解一下宿舍管理严格不严格，尤其是晚上用台灯、给电脑充电这些情况。")));
        list.add(new PostSeed("宿舍", "四人寝还是六人寝更容易相处？", List.of("目前还不知道自己会分到哪种宿舍。", "想提前做点心理准备，尤其是作息和公共卫生怎么协调。")));
        list.add(new PostSeed("宿舍", "军训期间住校需要准备哪些宿舍小物件？", List.of("已经准备了基础洗漱用品。", "还想补一些真正会高频用到的小东西，比如挂钩、收纳盒、蚊帐之类。")));
        list.add(new PostSeed("宿舍", "宿舍空调收费一般怎么分摊？", List.of("第一次住校，担心宿舍在电费和空调费上容易有矛盾。", "想问问大家通常怎么记账比较省事。")));

        list.add(new PostSeed("学习", "大一上高数如果基础一般，开学前需要先预习吗？", List.of("高中数学只能算中等。", "担心开学后节奏快跟不上，想知道先补哪些内容最有用。")));
        list.add(new PostSeed("学习", "早八真的会很难熬吗，怎么避免第一周就迟到？", List.of("看课表有好几天都是第一节课。", "想问问学长学姐有没有什么作息调整经验。")));
        list.add(new PostSeed("学习", "大学英语分级考试重要吗？", List.of("听说有的学院入学后会先考一次英语。", "这个成绩会影响后面选课或者四六级安排吗？")));
        list.add(new PostSeed("学习", "专业课教材有必要提前买吗？", List.of("担心开学后教材发得慢。", "但又怕自己提前买错版本浪费钱。")));
        list.add(new PostSeed("学习", "图书馆占座严不严，平时自习位置够吗？", List.of("比较在意学习环境。", "如果期中期末去得晚，是不是很难找到安静位置？")));

        list.add(new PostSeed("食堂", "学校食堂早餐窗口几点开始营业？", List.of("如果赶早八，想知道能不能来得及买到早餐。", "一般包子豆浆和面条这些窗口会开得早一点吗？")));
        list.add(new PostSeed("食堂", "食堂有没有适合减脂期吃的套餐？", List.of("打算开学后控制饮食。", "想问问有没有那种蔬菜和蛋白质比较均衡的窗口。")));
        list.add(new PostSeed("食堂", "饭卡充值是线上方便还是线下窗口方便？", List.of("第一次办校园卡。", "想知道如果临时余额不足，手机上能不能马上充进去。")));
        list.add(new PostSeed("食堂", "食堂晚上一般几点收摊，晚课后还能吃到什么？", List.of("担心上完课回来只剩泡面。", "想知道有没有夜宵窗口或者固定营业到比较晚的食堂。")));
        list.add(new PostSeed("食堂", "校内外卖能送到宿舍楼下吗？", List.of("有时候不想去食堂。", "想知道学校对外卖管理严不严，能不能直接送到楼下。")));

        list.add(new PostSeed("军训", "军训鞋垫真的很有必要准备吗？", List.of("看到很多攻略都说鞋垫重要。", "想知道是普通运动鞋垫就够，还是要买那种厚一点的减震款。")));
        list.add(new PostSeed("军训", "军训期间防晒到底要怎么补才有用？", List.of("担心晒黑也担心晒伤。", "有没有比较耐汗的防晒推荐，或者遮阳帽、防晒喷雾这些要不要带。")));
        list.add(new PostSeed("军训", "军训请假一般好请吗？", List.of("身体素质一般，有点担心高强度训练。", "如果中暑或者旧伤复发，需要提前准备什么材料吗？")));
        list.add(new PostSeed("军训", "军训期间手机管理严格吗？", List.of("想知道晚上回宿舍后能不能正常用手机。", "白天训练时是不是完全不能带在身上。")));
        list.add(new PostSeed("军训", "军训结束后会不会马上开始上课？", List.of("想提前规划一下节奏。", "如果军训和正式上课衔接很紧，生活用品是不是要更早准备齐。")));

        list.add(new PostSeed("其他", "报到当天家长可以陪着一起进宿舍吗？", List.of("家里人第一次送我来学校。", "想知道学校对家长进校、进宿舍有没有时间限制。")));
        list.add(new PostSeed("其他", "校园网是办宽带还是直接用手机流量更划算？", List.of("平时会用电脑查资料、看网课。", "想知道校园网稳定性怎么样，宿舍打游戏会不会卡。")));
        list.add(new PostSeed("其他", "新生体检一般都检查什么项目？", List.of("有点担心流程复杂。", "想问问需不需要空腹，或者要不要提前带照片、证件。")));
        list.add(new PostSeed("其他", "学院迎新群里消息太多，怎么筛重要通知？", List.of("刚进群就几百条消息。", "怕漏掉关键时间点，比如报到流程、选宿舍、军训集合这些。")));
        list.add(new PostSeed("其他", "校园卡、学生证、门禁卡会一起发吗？", List.of("不太清楚学校证件办理流程。", "想问问这些卡一般是报到当天拿，还是后面统一发。")));

        list.add(new PostSeed("宿舍", "宿舍洗衣机是扫码还是刷卡，排队严重吗？", List.of("想知道日常洗衣方便不方便。", "如果周末集中洗衣，是不是要排很久。")));
        list.add(new PostSeed("宿舍", "宿舍柜子空间够不够放冬天的衣服？", List.of("担心行李带多了没地方塞。", "如果空间有限，大家一般怎么收纳换季衣物。")));
        list.add(new PostSeed("宿舍", "舍友都爱打游戏到很晚怎么办？", List.of("怕自己比较早睡会不适应。", "想知道刚开学时怎么比较自然地沟通作息问题。")));
        list.add(new PostSeed("宿舍", "宿舍独卫和公共澡堂各有什么优缺点？", List.of("还不清楚自己宿舍条件。", "如果是公共澡堂，需要提前准备什么会方便一点。")));
        list.add(new PostSeed("宿舍", "新生报到那几天宿舍网络会不会很卡？", List.of("担心很多人同时连网。", "如果要在线填资料、抢选课，会不会受影响。")));

        list.add(new PostSeed("学习", "大一参加竞赛会不会太早？", List.of("有学长推荐早点接触一些比赛。", "但又怕刚入学课程都没适应，分不清主次。")));
        list.add(new PostSeed("学习", "课程平时分真的很重要吗？", List.of("听说大学不是只看期末考试。", "想知道平时作业、考勤和课堂表现占比一般大不大。")));
        list.add(new PostSeed("学习", "新生适合一开学就报名英语四级吗？", List.of("英语基础还行，但不确定大学节奏。", "想问问第一学期报四级是不是会太赶。")));
        list.add(new PostSeed("学习", "辅修和双学位是大几开始考虑比较合适？", List.of("对跨专业学习有点兴趣。", "但又担心自己主专业都还没学稳，怕压力太大。")));
        list.add(new PostSeed("学习", "课堂记笔记用平板还是纸质本更高效？", List.of("还在犹豫要不要买平板。", "希望能兼顾整理效率和复习体验。")));

        list.add(new PostSeed("食堂", "哪几个食堂窗口最适合第一次来学校踩点？", List.of("刚开学对学校还不熟。", "想先记住几家不太容易踩雷的窗口。")));
        list.add(new PostSeed("食堂", "食堂晚上的小火锅、麻辣烫这些排队夸张吗？", List.of("比较想知道热门窗口是不是常年排长队。", "如果去得晚，是不是就没什么好吃的了。")));
        list.add(new PostSeed("食堂", "学校里有适合一个人安静吃饭的地方吗？", List.of("有时候不太想在很吵的环境里吃。", "想知道有没有人少一点、座位宽松一点的食堂区域。")));
        list.add(new PostSeed("食堂", "周末食堂营业窗口会比平时少很多吗？", List.of("周末不想跑太远。", "想提前知道是不是需要调整吃饭时间。")));
        list.add(new PostSeed("食堂", "有没有食堂窗口支持少饭少面这种需求？", List.of("饭量不大。", "怕每次都打太多浪费，也想控制一下饮食。")));

        list.add(new PostSeed("军训", "军训服尺码偏大还是偏小？", List.of("担心领错尺码穿着不舒服。", "如果不合适，现场还能不能换。")));
        list.add(new PostSeed("军训", "军训站军姿的时候脚麻怎么办？", List.of("以前很少长时间站着。", "想知道有没有什么小技巧能稍微缓解一点。")));
        list.add(new PostSeed("军训", "军训期间能戴隐形眼镜吗？", List.of("平时戴框架有点不方便。", "但又担心太晒、太干容易不舒服。")));
        list.add(new PostSeed("军训", "军训结束会有汇演或者评比吗？", List.of("想知道是不是要准备统一动作和口号。", "如果有评比，平时训练是不是会更严格。")));
        list.add(new PostSeed("军训", "军训期间下雨的话一般改室内还是直接取消？", List.of("最近天气预报变化挺大。", "想知道学校一般怎么安排临时天气情况。")));

        list.add(new PostSeed("其他", "新生第一次去教学楼，导航好用吗？", List.of("校园有点大，怕第一周天天迷路。", "想知道学校地图、公众号导航这些准不准。")));
        list.add(new PostSeed("其他", "学校快递点离宿舍远吗，拿快递要排多久？", List.of("开学前后家里可能会寄很多东西。", "想问问快递点分布和高峰期取件体验。")));
        list.add(new PostSeed("其他", "报到当天行李太多，有没有校内小推车或者志愿者帮忙？", List.of("如果一个人带两个箱子，确实有点怕折腾。", "想提前了解有没有比较省力的办法。")));
        list.add(new PostSeed("其他", "入学后多久会开始选社团？", List.of("想多认识点人，也想参加一个偏兴趣的组织。", "不知道是不是一开学就会有很多宣讲和招新。")));
        list.add(new PostSeed("其他", "新生第一周最容易忽略但其实很重要的事是什么？", List.of("除了报到、军训和买生活用品之外。", "想听听大家踩过的坑，提前规避一下。")));
        return list;
    }

    private long nextId() {
        return nextId++;
    }

    private int countByQuery(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Timestamp queryTimestamp(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getTimestamp(1) : null;
        }
    }

    private String sqlQuote(String value) {
        return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'";
    }

    private String sqlTimestamp(LocalDateTime value) {
        return "'" + Timestamp.valueOf(value) + "'";
    }

    private record UserRow(long id, String username, String nickname, int role) {}

    private record ReplyRow(long id, long postId, long userId, long parentId) {}

    private record PostSeed(String tag, String title, List<String> paragraphs) {
        String htmlContent() {
            StringBuilder builder = new StringBuilder();
            for (String paragraph : paragraphs) {
                builder.append("<p>").append(paragraph).append("</p>");
            }
            return builder.toString();
        }
    }
}
