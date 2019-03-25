# Camera on Android

The main purpose of this sub-project is to use old Android mobile phone as monitor camera. However, I don't like writing/debuging Android's sucking Java's code. So that I make this sub-project as-simple-as-possible:

- Capure YUV from Camera (from Activity's SurfaceView indeed)
- Encode it as H.264 picture (no any other wrapper)
- Send it to home_server via TCP

