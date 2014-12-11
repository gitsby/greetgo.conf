package kz.greepto.gpen.drawport

import static java.lang.Math.min
import static java.lang.Math.max
import org.eclipse.swt.graphics.Point

class Rect {
  public int x = 0
  public int y = 0
  public int width = 0
  public int height = 0

  def Vec2 getPoint() { new Vec2(x, y) }

  def void setPoint(Vec2 point) {
    x = point?.x
    y = point?.y
  }

  def Size getSize() { new Size(width, height) }

  def void setSize(Size size) {
    width = size?.width
    height = size?.height
  }

  public static def Rect zero() { new Rect }

  public static def Rect copy(Rect r) { new Rect(r) }

  public static def Rect from(int x, int y, int width, int height) {
    return new Rect(x, y, width, height)
  }

  public static def Rect pointSize(Vec2 point, Size size) {
    return from(point?.x, point?.y, size?.width, size?.height)
  }

  private new() {
  }

  private new(int x, int y, int width, int height) {
    this.x = x
    this.y = y
    this.width = width
    this.height = height
  }

  private new(Rect a) {
    x = a.x
    y = a.y
    width = a.width
    height = a.height
  }

  def asd() {
    var a = zero
    var b = zero
    b += a
  }

  def Rect operator_add(Rect r) {
    var x1 = min(x, r.x)
    var x2 = max(x + width, r.x + r.width)

    var y1 = min(y, r.y)
    var y2 = max(y + height, r.y + r.height)

    x = x1
    y = y1
    width = x2 - x1
    height = y2 - y1

    return this
  }

  def Rect operator_plus(Rect r) {
    return copy(this) += r
  }

  def boolean contains(Point p) {
    if(p == null) return false

    if(p.x < x) return false
    if(p.y < y) return false

    if(p.x > x + width) return false
    if(p.y > y + height) return false

    return true
  }
}