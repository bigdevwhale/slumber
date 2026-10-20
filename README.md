<p align="center"><img src="docs/logo.png" width="128" alt="Spintop"></p>

<h1 align="center">Spintop</h1>

<p align="center"><b>A cosy lucid-dreaming trainer. In a dream, the top never falls.</b></p>

<p align="center"><a href="README.ru.md">Читать по-русски</a></p>

<p align="center">
  <img src="docs/screenshots/en/1-home.jpg" width="240">
  <img src="docs/screenshots/en/7-signs.jpg" width="240">
  <img src="docs/screenshots/en/9-ritual.jpg" width="240">
</p>

Spintop teaches you to notice, inside a dream, that you're dreaming. The practice has three parts, plus the top that keeps count.

## Morning: tell the dream

The morning note or the widget opens a dark, dim screen with red letters, so you can tell the dream half asleep.

- Type it or say it aloud.
- If you pause too long, the words slowly pale, the way the dream does. They are never actually lost.
- Then one question: *inside the dream, did you know it was a dream?* The answers are no, a glimmer, or yes.

## Dream signs

The app finds your **dream signs** in the journal by itself. These are the things that keep happening in your dreams and hardly ever when you're awake: school and exams, running late, lights that won't turn on, a childhood home, water. Nothing has to be tagged. You can also add a sign of your own, such as "the blue door".

## Day: reality checks

A few times a day, at moments you can't guess, a quiet **"Are you dreaming?"** arrives.

- Each one gives a check that works because dreams get it wrong: count your fingers, read some text twice, pinch your nose and try to breathe.
- Tap **Checked** right in the notification.
- The habit of asking carries over into sleep.

## Night: the ritual

Two minutes before bed, based on MILD, the best-tested way into lucid dreams:

1. Recall your last dream.
2. Look at tonight's sign.
3. Hold the top spinning while you repeat: *"Next time ‘school or an exam’ happens, I'll notice I'm dreaming."*

## The top

The top stands on a walnut desk under a lamp.

- The more you practise, the longer it spins: dreams remembered, checks, rituals, and above all glimmers and lucid dreams.
- Missed days loosen it a little and nothing more.
- Each lucid dream adds a brass ring.
- The morning after one, the top glows and doesn't fall all day.

## Private

There's no account, no internet permission and no tracking. Speaking a dream uses the phone's own speech recogniser. Every picture is drawn by the app in code.

## Build

```sh
./gradlew assembleDebug                 # app.slumber.dev
./gradlew assembleDebug -Pdemo          # app.slumber.demo with three weeks of practice
./gradlew testDebugUnitTest             # tests
./gradlew testDebugUnitTest -Prender --tests '*RenderArtScreens*' && python tools/shots.py   # screenshots
python tools/icon.py                    # docs/logo.png
```

Kotlin, Jetpack Compose, Room, DataStore. Android 9+.

## License

MIT
