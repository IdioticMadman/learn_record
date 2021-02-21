* 常见指令

  * v-model
  * v-if & v-else 
  * v-for
  * v-show
  * v-once
  * v-html
  * v-on 缩写 @
  * v-bind 缩写 ：

* Object.freeze()  会取消掉Vue的view-model之间的关联

  ```javascript
  var obj = {
    foo: 'bar'
  }
  
  Object.freeze(obj)
  
  new Vue({
    el: '#app',
    data: obj
  })
  ```

  ```javascript
  <div id="app">
    <p>{{ foo }}</p>
    <!-- 这里的 `foo` 不会更新！ -->
    <button v-on:click="foo = 'baz'">Change it</button>
  </div>
  ```

* 不能在生命周期函数上使用剪头函数，会导致this指向对象有问题

* 生命周期函数

  * beforeCreate created
  * beforeMount mounted
  * beforeUpdate updated
  * beforeDestory destoryed

  ![Vue生命周期](https://cn.vuejs.org/images/lifecycle.png)

* 方法，计算属性，侦听属性

  * 方法，即是function的集合
  * 计算属性会有缓存作用，且一定要有返回值。按需调用即可
  * 侦听属性，侦听属性的变化，做出对应的操作

* 条件渲染

  一般来说，`v-if` 有更高的切换开销，而 `v-show` 有更高的初始渲染开销。因此，如果需要非常频繁地切换，则使用 `v-show` 较好；如果在运行时条件很少改变，则使用 `v-if` 较好。

* 遍历，遍历对象时，value，key的形式

  ```javascript
      <div id="app">
        <h5>数组循环</h5>
        <ul>
          <li v-for="(item,index) in items">
            {{ parentMessage }} - {{ index }} - {{ item.message }}
          </li>
        </ul>
        <h5>对象循环</h5>
        <ul>
          <!-- value在前面，key在后面 -->
          <li v-for="(value,key) in object">{{ key }} : {{ value }}</li>
        </ul>
      </div>
      var app = new Vue({
        el: "#app",
        data: {
          items: [{ message: "foo" }, { message: "bar" }],
          object: {
            firstName: "xiong",
            lastName: "robert",
            sex: "男",
            age: 18
          }
        }
      });
  ```

* template多个标签组成模板