# Multimedia_Player
**Extracting indexes from video along with interactive exploration**

Interactive A/V Player and Interface
A simple functional interface that shows the extracted table of contents of the video listed along with the video playing on the right. Support for play pause and
stop provided.

![image](https://github.com/sanmitsahu/Multimedia_Player/assets/58842794/1fe79c5a-d627-4171-9041-605ded44c846)


The player is able to play, pause, and stop the video (and its corresponding audio). Pausing and playing resumes the video from the paused time. Stopping and
playing restarts the video from the beginning of the current selected shot.

There is an area where a hierarchical video table of contents is visible. This is the output of your processing. The shots are displayed and elements are “selectable”. 

The hierarchical table of content shows a breakdown of the video at various
levels:
1. Sequence/scene level
2. Individual shot level
3. and if the shot is varied properties, then at the subshot level.
Interactively selecting a segment highlights the segment. 

If the player is stopped, the upon playing the video should start playing from the beginning of that segment. If the player is playing a different segment is selected, the player should update by playing the new selected segment. 
This way you can bounce around the content by selecting various shots

As the video plays continuously the highlighted segment selection (scene, sequence, shot) should correspondingly update.

![image](https://github.com/sanmitsahu/Multimedia_Player/assets/58842794/c658f3e4-4245-47bc-9edb-943dae39fae7)

The anatomy of a video might help better understand how to build hierarchies.
• Frame: a single still image from a video, eg NTSC - 30 frames/second, film – 24
frames/second
• Shot: sequence of frames recorded in a single camera operation
• Sequence or Scenes: collection of shots forming a semantic unit which
conceptually may be shot at a single time and place
