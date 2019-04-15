1. const和final

   const修饰为编译时常量。final为运行时常量。都在运行时不可变，但是const是在编译期间就确定了值。

2. 命名的构造函数

   <className>.<name>  类名.名字 定义构造函数的名字

   ```dart
   class Point {
     num x, y;
   
     Point(this.x, this.y);
   
     // Named constructor
     Point.origin() {
       x = 0;
       y = 0;
     }
   }
   ```

3. new 关键字，在dart2中，可写可不写

4. extends , implements, with