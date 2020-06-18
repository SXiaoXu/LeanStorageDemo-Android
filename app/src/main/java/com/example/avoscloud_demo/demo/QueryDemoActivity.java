package com.example.avoscloud_demo.demo;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.avoscloud_demo.DemoBaseActivity;
import com.example.avoscloud_demo.DemoUtils;
import com.example.avoscloud_demo.Student;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class QueryDemoActivity extends DemoBaseActivity {

  public void testBasicQuery() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    List<Student> students = query.find();
    log("找回了一组 Student:" + prettyJSON(students));
    logThreadTips();
  }

  public void testGetFirstObject() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    //query.includeACL(true);
    Student student = query.getFirst();
    log("找回了最近更新的第一个 Student" + prettyJSON(student));
  }

  public void testLimit() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereLessThanOrEqualTo(AVObject.UPDATED_AT, new Date());
    query.limit(2);
    List<Student> students = query.find();
    log("找回了两个学生:" + prettyJSON(students));
  }

  public void testSkip() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.orderByDescending("createdAt");
    query.skip(3);
    Student first = query.getFirst();
    log("找回了倒数第四个创建的 Student:" + first);
  }

  public void testAndQuery() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereNotEqualTo(Student.NAME, "Mike");

    // 默认就是 And
    query.whereStartsWith(Student.NAME, "M");

    List<Student> students = query.find();
    log("名字不是 Mike 但 M 开头的学生：");
    logObjects(students, Student.NAME);
  }

  public void testOrQuery() throws AVException {
    AVQuery<Student> query1 = AVQuery.getQuery(Student.class);
    query1.whereEqualTo(Student.NAME, "Mike");

    AVQuery<Student> query2 = AVQuery.getQuery(Student.class);
    query2.whereStartsWith(Student.NAME, "J");

    List<AVQuery<Student>> queries = new ArrayList<>();
    queries.add(query1);
    queries.add(query2);

    AVQuery<Student> query = AVQuery.or(queries);
    List<Student> students = query.find();
    log("名字是 Mike 或 J 开头的学生：");
    logObjects(students, Student.NAME);
  }

  public void testAscending() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.orderByAscending(Student.CREATED_AT)
        .limit(5);
    List<Student> students = query.find();
    log("找出了5个最早创建的学生");
    logObjects(students, Student.CREATED_AT);
  }

  public void testSecondOrder() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.orderByDescending(Student.NAME)
        .addDescendingOrder(Student.AGE)
        .limit(5);
    List<Student> students = query.find();
    log("找回了名字排序靠后，年龄最大的五个学生 ");
    logObjects(students, Student.NAME);
    logObjects(students, Student.AGE);
  }

  public void testArraySize() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereSizeEqual(Student.HOBBIES, 2)
        .limit(10);
    List<Student> students = query.find();
    log("找回了爱好有两个的学生：");
    logObjects(students, Student.HOBBIES);
  }

  public void testContainedIn() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereContainedIn(Student.NAME, Arrays.asList("Mike", "Jane"));
    List<Student> students = query.find();
    log("找回了名字是 Mike 或 Jane 的学生");
    logObjects(students, Student.NAME);
  }

  public void testContainsAll() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereContainsAll(Student.HOBBIES, Arrays.asList("swimming", "running"));
    //query.includeACL(true);
    List<Student> students = query.find();
    log("找回了爱好至少有 swimming 和 running 的学生：");
    logObjects(students, Student.HOBBIES);
  }

  public void testLimitSize() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    // 最大 1000，默认 100
    query.limit(1000);
    List<Student> students = query.find();
    log("找回了最多 1000 个学生，实际上有 %d 个", students.size());
  }

  public void testRegex() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereMatches(Student.NAME, "^M.*");
    List<Student> students = query.find();
    log("名字满足正则表达式 ^M.* 的学生：");
    logObjects(students, Student.NAME);
  }

  public void testOneKeyMultipleCondition() throws AVException {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereStartsWith(Student.NAME, "M")
        .whereEndsWith(Student.NAME, "e")
        .whereContains(Student.NAME, "i");
    List<Student> students = query.find();
    log("名字以 M 开头、e 结尾、含有 i 的学生：");
    logObjects(students, Student.NAME);
  }

  public void testLastModifyEnabled() throws AVException {
    // 应该放在 Application 的 onCreate 中，开启全局省流量模式
    AVOSCloud.setLastModifyEnabled(true);

    Student student = getFirstStudent();

    // 此处服务器应该返回了所有数据
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    Student student1 = q.get(student.getObjectId());
    log("从服务器获取了对象：" + prettyJSON(student1));

    // 客户端把该对象的 udpatedAt 传给服务器，服务器判断对象未改变，于是返回 304 和空数据，客户端返回本地缓存的数据，节省流量
    Student student2 = q.get(student.getObjectId());
    log("对象的更新时间戳和服务器的愈合，从本地获取了对象：" + prettyJSON(student2));
  }

  public void testLastModifyEnabled2() throws AVException {
    // 应该放在 Application 的 onCreate 中，开启全局省流量模式
    AVOSCloud.setLastModifyEnabled(true);

    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.limit(5);
    // 此处服务器应该返回了所有数据
    List<Student> students = q.find();
    log("从服务器获取了对象：" + prettyJSON(students));

    // 服务器记录表的修改时间，如果两次查询之间表未被修改且参数一样，则以下查询将从本地缓存获取数据
    List<Student> students1 = q.find();
    log("前后之间，Student 表未被改动，从本地获取了对象：" + prettyJSON(students1));
  }

  public void testQueryPolicyCacheThenNetwork() {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
    // 单位毫秒
    q.setMaxCacheAge(1000 * 60 * 60); // 一小时
    q.limit(1);
    q.findInBackground(new FindCallback<Student>() {
      int count = 0;

      @Override
      public void done(List<Student> list, AVException e) {
        if (count == 0) {
          log("第一次从缓存中获取了结果：" + prettyJSON(list));
        } else {
          log("第二次从网络获取了结果：" + prettyJSON(list));
        }
        count++;
      }
    });
  }

  public void testQueryPolicyCacheElseNetwork() throws AVException {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
    // 单位毫秒
    q.setMaxCacheAge(1000 * 60 * 60); // 一小时
    q.limit(1);
    if (q.hasCachedResult()) {
      log("有本地缓存，将从本地获取");
    } else {
      log("无本地缓存，将从服务器获取");
    }
    List<Student> students = q.find();
    log("查找结果为：" + prettyJSON(students));
  }

  public void testQueryPolicyNetworkElseCache() throws AVException {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    // 单位毫秒
    q.setMaxCacheAge(1000 * 60 * 60); // 一小时
    q.limit(1);
    if (q.hasCachedResult()) {
      log("有本地缓存，无网络时将从本地获取");
    } else {
      log("无本地缓存，将从服务器获取");
    }
    List<Student> students = q.find();
    log("查找结果为：" + prettyJSON(students));
    log("此时有本地缓存了，关闭网络时运行此例子，将从本地缓存中获取结果");
  }

  public void testQueryPolicyNetworkOnly() throws AVException {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
    // 单位毫秒
    q.setMaxCacheAge(1000 * 60 * 60); // 一小时
    q.limit(1);
    if (q.hasCachedResult()) {
      log("有本地缓存，但无视之");
    } else {
      log("无本地缓存，也无视之");
    }
    List<Student> students = q.find();
    log("从网络获取了结果：" + prettyJSON(students));
    log("NETWORK_ONLY 策略和默认的 IGNORE_CACHE 策略不同的是，前者会把结果保存在本地");
  }

  public void testQueryPolicyCacheOnly() throws AVException {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    // 单位毫秒
    q.setMaxCacheAge(1000 * 60 * 60); // 一小时
    q.limit(1);
    if (q.hasCachedResult()) {
      log("有本地缓存，将从本地获取结果");
    } else {
      log("无本地缓存，将抛出异常，请先运行上一个例子，从网络获取结果保存到本地");
    }
    List<Student> students = q.find();
    log("从本地缓存获取了结果：" + prettyJSON(students));
  }

  public void testQueryPolicyIngoreCache() throws AVException {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    log("此策略才网络获取结果，并不保存结果到本地");
    q.setCachePolicy(AVQuery.CachePolicy.IGNORE_CACHE);
    // 单位毫秒
    q.setMaxCacheAge(1000 * 60 * 60); // 一小时
    q.limit(1);
    List<Student> students = q.find();
    log("从网络缓存获取了结果：" + prettyJSON(students));
  }

  public void clearQueryCache() {
    AVQuery<Student> q = AVQuery.getQuery(Student.class);
    q.limit(1);
    q.clearCachedResult();
    log("已删除 limit=1 className= Student 的查询缓存");
  }

  public void clearAllCache() {
    AVQuery.clearAllCachedResults();
    log("已删除所有的缓存");
  }

  // create an object and query it.
  public void testObjectQuery() throws AVException {
    AVObject person1 = AVObject.create("Person");
    person1.put("gender", "Female");
    person1.put("name", "Cake");
    person1.save();

    AVObject person2 = AVObject.create("Person");
    person2.put("gender", "Male");
    person2.put("name", "Man");
    person2.save();

    AVObject something = AVObject.create("Something");
    something.put("belongTo", "Cake");
    something.put("city", "ChangDe");
    something.save();

    AVObject another = AVObject.create("Something");
    another.put("belongTo", "Man");
    another.put("city", "Beijing");
    another.save();

    AVQuery q1 = AVQuery.getQuery("Person");
    q1.whereEqualTo("gender", "Female");

    AVQuery q2 = AVQuery.getQuery("Something");
    q2.whereMatchesKeyInQuery("belongTo", "name", q1);
    List<AVObject> objects = q2.find();
    Assert.assertTrue(objects.size() > 0);
    for (AVObject obj : objects) {
      Assert.assertTrue(obj.getString("belongTo").equals("Cake"));
    }

    AVQuery q3 = AVQuery.getQuery("Something");
    q3.whereDoesNotMatchKeyInQuery("belongTo", "name", q1);
    List<AVObject> list = q3.find();
    Assert.assertTrue(list.size() > 0);
    for (AVObject obj : list) {
      Assert.assertFalse(obj.getString("belongTo").equals("Cake"));
    }
  }

  public void testUserQuery() throws AVException {
    String lastString = null;
    // signup some test user
    for (int i = 0; i < 10; ++i) {
      AVUser user = new AVUser();
      user.setUsername(DemoUtils.getRandomString(10));
      user.setPassword(DemoUtils.getRandomString(10));
      user.signUp();
      Assert.assertFalse(user.getObjectId().isEmpty());
      lastString = user.getUsername();
    }

    AVQuery currentQuery = AVUser.getQuery();
    AVQuery innerQuery = AVUser.getQuery();
    innerQuery.whereContains("username", lastString);
    currentQuery.whereMatchesKeyInQuery("username", "username", innerQuery);

    List<AVUser> users = currentQuery.find();
    Assert.assertTrue(users.size() == 1);
    for (AVUser resultUser : users) {
      Assert.assertTrue(resultUser.getUsername().equals(lastString));
    }
  }
}
