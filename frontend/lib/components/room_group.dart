import 'package:dodo/components/certification.dart';
import 'package:dodo/components/room_chatscreen.dart';
import 'package:dodo/components/roomuser_list.dart';
import 'package:dodo/components/roomset_basic.dart';
import 'package:dodo/components/roomset_manager.dart';
import 'package:flutter/material.dart';
import 'package:dodo/const/colors.dart';

class room_group extends StatefulWidget {
  final String room_title;
  final int room_id;
  final int room_mem;
  final int room_maxmem;
  final bool canChat;
  final bool is_manager;
  const room_group(
      {super.key,
      required this.room_title,
      required this.room_id,
      required this.room_mem,
      required this.room_maxmem,
      required this.canChat,
      required this.is_manager});

  @override
  State<room_group> createState() => _roomMainState();
}

class _roomMainState extends State<room_group> {
  final userList = [
    {
      "user_name": "User1",
      "user_id": 1,
      "user_img": "assets/images/cook.jpg",
      "success": 0,
      "wait": 1,
      "max": 3,
      "certification": false,
    },
    {
      "user_name": "User2",
      "user_id": 2,
      "user_img": "assets/images/cook.jpg",
      "success": 0,
      "wait": 2,
      "max": 3,
      "certification": false,
    },
    {
      "user_name": "User3",
      "user_id": 3,
      "user_img": "assets/images/cook.jpg",
      "success": 1,
      "wait": 1,
      "max": 3,
      "certification": false,
    },
    {
      "user_name": "User4",
      "user_id": 4,
      "user_img": "assets/images/cook.jpg",
      "success": 3,
      "wait": 0,
      "max": 3,
      "certification": true,
    },
    {
      "user_name": "User5",
      "user_id": 5,
      "user_img": "assets/images/cook.jpg",
      "success": 0,
      "wait": 3,
      "max": 3,
      "certification": false,
    },
    {
      "user_name": "User6",
      "user_id": 6,
      "user_img": "assets/images/cook.jpg",
      "success": 3,
      "wait": 0,
      "max": 3,
      "certification": true,
    },
  ];

  @override
  Widget build(BuildContext context) {
    String room_title = widget.room_title;
    int room_id = widget.room_id;
    int room_mem = widget.room_mem;
    bool canChat = widget.canChat;
    bool is_manager = widget.is_manager;

    return Scaffold(
      appBar: _roomMainAppBar(room_title, manager: is_manager),
      backgroundColor: LIGHTGREY,
      floatingActionButton:
          canChat ? chattingRoom(room_title, is_manager, room_id, 1) : null,
      body: SingleChildScrollView(
        child: Column(
          children: [
            const SizedBox(
              height: 25,
            ),
            // 목표 기한
            _progressBar(),
            Container(
              margin: const EdgeInsets.all(20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  // 도전 완료 사용자 수
                  _certificated_person(room_mem),
                  // 인증하기 버튼
                  _certification_button(),
                ],
              ),
            ),
            RoomUserList(),
          ],
        ),
      ),
    );
  }

  PreferredSizeWidget _roomMainAppBar(String title, {bool manager = false}) {
    return PreferredSize(
      preferredSize: const Size.fromHeight(80),
      child: Container(
        width: 390,
        height: 80,
        // Border Line
        decoration: const ShapeDecoration(
          shape: RoundedRectangleBorder(
            side: BorderSide(
              width: 2,
              strokeAlign: BorderSide.strokeAlignOutside,
              color: Color(0x7F414C58),
            ),
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            AppBar(
              backgroundColor: LIGHTGREY,
              leading: const BackButton(
                color: PRIMARY_COLOR,
              ),
              title: Text(
                title,
                style: const TextStyle(
                  color: PRIMARY_COLOR,
                  fontFamily: "bm",
                  fontSize: 30,
                ),
              ),
              actions: [
                IconButton(
                  onPressed: () {},
                  icon: const Icon(
                    Icons.notifications_outlined,
                    color: PRIMARY_COLOR,
                    size: 35,
                  ),
                ),
                IconButton(
                  onPressed: () {
                    if (manager) {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => RoomSetting_Manager(),
                        ),
                      );
                    } else {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => RoomSetting_Basic(),
                        ),
                      );
                    }
                  },
                  icon: const Icon(
                    Icons.settings_outlined,
                    color: PRIMARY_COLOR,
                    size: 35,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Container _progressBar() {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        color: const Color.fromARGB(199, 193, 208, 214),
      ),
      height: 100,
      margin: const EdgeInsets.fromLTRB(28, 0, 28, 0),
      padding: const EdgeInsets.fromLTRB(30, 10, 30, 10),
      child: Row(
        children: [
          const Icon(
            Icons.lightbulb_outline_rounded,
            color: POINT_COLOR,
            size: 40,
          ),
          const SizedBox(width: 20),
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              const Row(
                children: [
                  Text(
                    "목표를 ",
                    style: TextStyle(
                      color: POINT_COLOR,
                      fontFamily: 'bm',
                      fontSize: 21,
                    ),
                  ),
                  Text(
                    "70%",
                    style: TextStyle(
                      color: PRIMARY_COLOR,
                      fontFamily: 'bm',
                      fontSize: 25,
                    ),
                  ),
                  Text(
                    " 달성했어요!",
                    style: TextStyle(
                      color: POINT_COLOR,
                      fontFamily: 'bm',
                      fontSize: 21,
                    ),
                  ),
                ],
              ),
              // progress bar
              Stack(
                children: [
                  Container(
                    height: 15,
                    width: MediaQuery.of(context).size.width * 0.6,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(20),
                      color: Colors.white,
                    ),
                  ),
                  Container(
                    height: 15,
                    width: MediaQuery.of(context).size.width * 0.42,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(20),
                      color: PRIMARY_COLOR,
                    ),
                  ),
                ],
              ),
              // D-day
              const SizedBox(
                height: 2,
              ),
              Text(
                "목표까지 D-30",
                style: const TextStyle(
                  color: POINT_COLOR,
                  fontFamily: 'bm',
                  fontSize: 18,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Column _certificated_person(int? mem) {
    return Column(
      children: [
        const Text(
          "목표를 위해\n도전한 사람은?",
          textAlign: TextAlign.center,
          style: TextStyle(
              color: POINT_COLOR, fontSize: 21, fontWeight: FontWeight.bold),
        ),
        Row(
          children: [
            Text(
              "1/$mem",
              style: const TextStyle(
                  color: POINT_COLOR,
                  fontSize: 48,
                  fontWeight: FontWeight.bold),
            ),
            const Text(
              " 명",
              style: TextStyle(
                  color: POINT_COLOR,
                  fontSize: 20,
                  fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ],
    );
  }

  SizedBox _certification_button() {
    return SizedBox(
      height: 100,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: PRIMARY_COLOR,
          elevation: 4,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          padding: const EdgeInsets.all(10),
        ),
        onPressed: () {
          tododialog(context);
        },
        child: const Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.check_circle_outline,
              color: Colors.white,
              size: 40,
            ),
            SizedBox(
              width: 5,
            ),
            Text(
              '인증하기',
              style: TextStyle(
                fontSize: 30,
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Container chattingRoom(String title, bool manager, int roomID, int userID) {
    return Container(
      height: 80,
      width: 80,
      margin: const EdgeInsets.all(5),
      child: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => RoomChatScreen(
                room_title: title,
                is_manager: manager,
                roomId: roomID,
                userId: userID,
              ),
            ),
          );
        },
        backgroundColor: PRIMARY_COLOR,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(100),
          side: const BorderSide(
            color: Color(0xffefefef),
            width: 3,
          ),
        ),
        child: const Icon(
          Icons.chat,
          color: Color(0xffefefef),
          size: 35,
        ),
      ),
    );
  }
}

void tododialog(context) {
  showDialog(
    context: context,
    builder: (context) {
      return Dialog(
          child: SizedBox(
        width: 300,
        //height: 300,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            //여기는 그 인증하는 곳으로 이어졌으면 함. 밑에는 일단 예시
            const Text("사진 넣으셨는지~"),
            Certification("test"),
            const SizedBox(
              height: 100,
            ),
            IconButton(
              onPressed: () {
                //팝업 지우기
                Navigator.of(context).pop();
              },
              icon: const Icon(Icons.close),
            )
          ],
        ),
      ));
    },
  );
}