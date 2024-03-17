import 'package:dodo/const/colors.dart';
import 'package:dodo/screen/findpass_screen.dart';
import 'package:dodo/screen/signup_screen.dart';
import 'package:flutter/material.dart';

//이건 너가 만든 대로 넣으면 돼!
class loginPage extends StatelessWidget {
  loginPage({super.key});

  GlobalKey<FormState> _globalKey = GlobalKey<FormState>();
  TextEditingController _emailEditingcontroller = TextEditingController();
  TextEditingController _passwordEditingcontroller = TextEditingController();
  AutovalidateMode _autovalidateMode = AutovalidateMode.disabled;
  bool? _isEnabled = true;

  void dispose() {
    _emailEditingcontroller.dispose();
    _passwordEditingcontroller.dispose();
    dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(80),
        child: Container(
          width: 100,
          height: 100,
          alignment: Alignment.topRight,
          padding: const EdgeInsets.fromLTRB(0, 30, 30, 0),
          child: const Image(
            image: AssetImage('assets/images/logo.png'),
            width: 110,
            height: 110,
          ),
        ),
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 50),
                child: Form(
                  child: ListView(
                    shrinkWrap: true,
                    children: [
                      const Text(
                        '로그인',
                        style: TextStyle(
                          fontSize: 30,
                          fontFamily: "kcc",
                        ),
                      ),

                      const SizedBox(
                        height: 30,
                      ),

                      // 구글 로그인
                      ElevatedButton(
                        onPressed: () {},
                        style: ElevatedButton.styleFrom(
                          foregroundColor: Colors.white,
                          textStyle: const TextStyle(fontSize: 20),
                          backgroundColor: const Color(0xff1CB5E0),
                          padding: const EdgeInsets.symmetric(vertical: 15),
                          minimumSize: const Size(140, 70),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20),
                          ),
                        ),
                        child: const Text('Google로 로그인'),
                      ),

                      const SizedBox(
                        height: 30,
                      ),

                      // 또는
                      const Text(
                        '또는',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w400,
                        ),
                      ),

                      const SizedBox(
                        height: 30,
                      ),

                      //이메일
                      TextFormField(
                        keyboardType: TextInputType.emailAddress,
                        style: const TextStyle(
                          color: Color(0xff4f4f4f),
                          fontSize: 20,
                        ),
                        decoration: InputDecoration(
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(20),
                              borderSide: BorderSide.none,
                            ),
                            labelText: '이메일 주소',
                            labelStyle: const TextStyle(
                                color: Color(0xff4f4f4f), fontSize: 18),
                            // prefixIcon: Icon(Icons.email_rounded),
                            filled: true,
                            fillColor: const Color(0xffEDEDED)),
                        validator: (value) {
                          //아무것도 입력하지 않았을 때
                          //공백만 입력했을 때
                          //이메일 형식이 아닐 때
                          // if (value == null ||
                          //     value.trim().isEmpty ||
                          //     isEmail(value.trim())) {
                          //   return '이메일을 입력해주세요';
                          // }
                          return null;
                        },
                      ),

                      const SizedBox(height: 30),

                      //패스워드
                      TextFormField(
                        enabled: _isEnabled,
                        controller: _passwordEditingcontroller,
                        obscureText: true,
                        style: const TextStyle(
                          color: Color(0xff4f4f4f),
                          fontSize: 20,
                        ),
                        decoration: InputDecoration(
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(20),
                              borderSide: BorderSide.none,
                            ),
                            // focusedBorder: OutlineInputBorder(
                            //   borderRadius: BorderRadius.circular(20),
                            //   borderSide: BorderSide(
                            //     color: Color(0xffEDEDED),
                            //     width: 25.0,
                            //   ),
                            // ),
                            labelText: '비밀번호',
                            labelStyle: const TextStyle(
                                color: Color(0xff4f4f4f), fontSize: 18),
                            // prefixIcon: Icon(Icons.lock),
                            filled: true,
                            fillColor: const Color(0xffEDEDED)),
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return '패스워드를 입력해주세요';
                          }
                          if (value.length < 6) {
                            return '6글자 이상 입력해';
                          }
                          return null;
                        },
                      ),

                      const SizedBox(height: 30),

                      // 로그인
                      ElevatedButton(
                        onPressed: () {},
                        style: ElevatedButton.styleFrom(
                          foregroundColor: Colors.white,
                          textStyle: const TextStyle(fontSize: 20),
                          backgroundColor: const Color(0xff1CB5E0),
                          padding: const EdgeInsets.symmetric(vertical: 15),
                          minimumSize: const Size(140, 70),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20),
                          ),
                        ),
                        child: const Text('로그인'),
                      ),

                      const SizedBox(
                        height: 20,
                      ),

                      TextButton(
                        onPressed: () {
                          Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (context) => SignupPage()));
                        },
                        style: TextButton.styleFrom(
                          foregroundColor: const Color(0xff464646),
                        ),
                        child: const Text(
                          '아이디가 없나요? 회원가입하기',
                          style: TextStyle(fontSize: 18),
                        ),
                      ),

                      const SizedBox(
                        height: 5,
                      ),

                      TextButton(
                        onPressed: () {
                          Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (context) => const findpassPage()));
                        },
                        style: TextButton.styleFrom(
                          foregroundColor: const Color(0xff464646),
                        ),
                        child: const Text(
                          '비밀번호를 잃어버렸나요?',
                          style: TextStyle(fontSize: 18),
                        ),
                      ),

                      // 하단 여백
                      const SizedBox(
                        height: 50,
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
