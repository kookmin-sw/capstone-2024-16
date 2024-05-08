import 'package:dodo/const/colors.dart';
import 'package:flutter/material.dart';
import 'package:matrix_gesture_detector/matrix_gesture_detector.dart';
import 'package:vector_math/vector_math_64.dart' as vector;
import 'package:shared_preferences/shared_preferences.dart';

// 좌표를 저장하는 함수
Future<void> saveCoordinates(double x, double y) async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.setDouble('xCoordinate', x);
  await prefs.setDouble('yCoordinate', y);
}

// 저장된 좌표를 불러오는 함수
Future<List<double>> getCoordinates() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  double x = prefs.getDouble('xCoordinate') ?? 0.0;
  double y = prefs.getDouble('yCoordinate') ?? 0.0;
  return [x, y];
}

class overview_sea extends StatefulWidget {
  final int c_id;
  const overview_sea(this.c_id);
  @override
  _overview_seaState createState() => _overview_seaState();
}

class _overview_seaState extends State<overview_sea> {
  Matrix4? matrix;
  late ValueNotifier<Matrix4?> notifier;
  late Boxer boxer;

  @override
  void initState() {
    super.initState();
    matrix = Matrix4.identity();
    notifier = ValueNotifier(matrix);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: LayoutBuilder(
        builder: (ctx, constraints) {
          var width = constraints.biggest.width / 10;
          var height = constraints.biggest.height / 10;
          var dx = (constraints.biggest.width - width) / 2;
          var dy = (constraints.biggest.height - height) / 2;
          matrix!.leftTranslate(dx, dy);
          boxer = Boxer(Offset.zero & constraints.biggest,
              Rect.fromLTWH(0, 0, width, height));

          return MatrixGestureDetector(
            shouldRotate: false,
            onMatrixUpdate: (m, tm, sm, rm) {
              matrix = MatrixGestureDetector.compose(matrix!, tm, sm, null);
              boxer.clamp(matrix!);
              notifier.value = matrix;

              final tx = matrix!.storage[12]; // x 좌표
              final ty = matrix!.storage[13]; // y 좌표
              print('x 좌표: $tx, y 좌표: $ty');
            },
            child: Stack(children: [
              Image.asset(
                "assets/images/sea.png",
                fit: BoxFit.cover,
              ),
              AnimatedBuilder(
                builder: (ctx, child) {
                  return Transform(
                    transform: matrix!,
                    child: Image.asset(
                      "assets/images/turtle.png",
                      fit: BoxFit.cover,
                      scale: 2,
                    ),
                  );
                },
                animation: notifier,
              ),
              //취소 저장 버튼
              Align(
                alignment: Alignment.bottomCenter,
                child: Container(
                  padding: const EdgeInsets.all(20.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Expanded(
                        flex: 1,
                        child: OutlinedButton(
                          onPressed: () {
                            Navigator.pop(context);
                          },
                          style: OutlinedButton.styleFrom(
                              backgroundColor: Colors.white,
                              elevation: 2,
                              foregroundColor: Colors.black,
                              shadowColor: Colors.black,
                              side: const BorderSide(color: PRIMARY_COLOR),
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10))),
                          child: const Text("취소",
                              style: TextStyle(
                                  color: PRIMARY_COLOR,
                                  fontFamily: "bm",
                                  fontSize: 20)),
                        ),
                      ),
                      const SizedBox(
                        width: 10,
                      ),
                      Expanded(
                        flex: 2,
                        child: OutlinedButton(
                          onPressed: () {
                            // Navigator.push(context,
                            //     MaterialPageRoute(builder: (context) => AIroom_cr3()));
                          },
                          style: ElevatedButton.styleFrom(
                              backgroundColor: PRIMARY_COLOR,
                              elevation: 2,
                              foregroundColor: Colors.black,
                              shadowColor: Colors.black,
                              side: BorderSide(color: PRIMARY_COLOR),
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10))),
                          child: const Text(
                            "저장",
                            style: TextStyle(
                                color: Colors.white,
                                fontFamily: "bm",
                                fontSize: 20),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              )
            ]),
          );
        },
      ),
    );
  }
}

class Boxer {
  final Rect bounds;
  final Rect src;
  late Rect dst;

  Boxer(this.bounds, this.src);

  void clamp(Matrix4 m) {
    dst = MatrixUtils.transformRect(m, src);
    if (bounds.left <= dst.left &&
        bounds.top <= dst.top &&
        bounds.right >= dst.right &&
        bounds.bottom >= dst.bottom) {
      return;
    }

    if (dst.width > bounds.width || dst.height > bounds.height) {
      Rect intersected = dst.intersect(bounds);
      FittedSizes fs = applyBoxFit(BoxFit.contain, dst.size, intersected.size);

      vector.Vector3 t = vector.Vector3.zero();
      intersected = Alignment.center.inscribe(fs.destination, intersected);
      if (dst.width > bounds.width)
        t.y = intersected.top;
      else
        t.x = intersected.left;

      var scale = fs.destination.width / src.width;
      vector.Vector3 s = vector.Vector3(scale, scale, 0);
      m.setFromTranslationRotationScale(t, vector.Quaternion.identity(), s);
      return;
    }

    if (dst.left < bounds.left) {
      m.leftTranslate(bounds.left - dst.left, 0.0);
    }
    if (dst.top < bounds.top) {
      m.leftTranslate(0.0, bounds.top - dst.top);
    }
    if (dst.right > bounds.right) {
      m.leftTranslate(bounds.right - dst.right, 0.0);
    }
    if (dst.bottom > bounds.bottom) {
      m.leftTranslate(0.0, bounds.bottom - dst.bottom);
    }
  }
}