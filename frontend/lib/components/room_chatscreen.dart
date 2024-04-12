import 'package:dodo/const/colors.dart';
import 'package:flutter/material.dart';

class RoomChatScreen extends StatelessWidget {
  final String room_title;
  final bool is_manager;
  const RoomChatScreen(
      {super.key, required this.room_title, required this.is_manager});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _roomMainAppBar(room_title, manager: is_manager),
      backgroundColor: LIGHTGREY,
      resizeToAvoidBottomInset: true,
      body: Column(
        children: [
          Expanded(
            child: Text(
              '채팅방입니다.',
              style: TextStyle(
                fontSize: 80,
              ),
            ),
          ),
          _TextInputForm(),
        ],
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
                "$title 채팅방",
                style: const TextStyle(
                  color: PRIMARY_COLOR,
                  fontFamily: "bm",
                  fontSize: 30,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _TextInputForm extends StatelessWidget {
  const _TextInputForm({super.key});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      bottom: true,
      child: Container(
        color: PRIMARY_COLOR,
        margin: const EdgeInsets.all(10),
        height: 50,
        width: MediaQuery.of(context).size.width,
        child: TextFormField(),
      ),
    );
  }
}
