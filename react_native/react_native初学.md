* Props属性：通过构造函数设置一些初始值

* State
  * 一切界面变化都是`状态state变化`
  * `state`的修改必须通过`setState()`方法
    * this.state.likes = 100; // 这样的`直接赋值修改无效！`
    * setState 是一个 merge 合并操作，只修改指定属性，不影响其他属性
    * setState 是`异步`操作，修改`不会马上生效`
* 样式，借鉴 CSS 中的“层叠”做法（即后声明的属性会覆盖先声明的同名属性）
* 尺寸大小：
  * 通过，width和height设定具体值
  * 通过flex属性，设置剩余空间的占比。如果父容器没有设置flex或者固定高度，子控件只设置flex不生效

* flex布局：
  * flexDirection 主轴方向
    * row：横向排列（左->右）
    * row-revese：横向排列（右->左）
    * column：竖向排列
  * justifyContent: 主轴方向上，元素的怎么排列
  * 