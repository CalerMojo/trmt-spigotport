<p align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/177c11b808636328e9360ff476341a49e7af3167.png" alt="TRMT cover">
</p>

# The Roads More Travelled [TRMT] (Plugin Port)

A dynamic terrain plugin that adds a gradual erosion system to the game, slowly transforming the routes you travel more often through into beautiful and realistic (yet still vanilla-friendly!) looking paths. Make your world feel more immersive by etching the story of your explorations onto the landscape.

## Logic
- Grass blocks, dirt blocks, and sand blocks accumulate an erosion index everytime they're are stepped on. Neighbouring blocks accumulate a fraction of this index as well.
- Upon reaching a threshold, each one of these blocks transforms into their eroded variant progressing through different stages of erosion until reaching a final stage. Leaves blocks and vegeation are trampled when reaching their threshold.
- Erosion thresholds are randomly determined to each block type, making erosion happen in a more organic, less determnistic way.
- *Erosion can be triggered by players, players on mounts or mobs on leashes. The strength of the erosion is controlled by a configurable multiplier. By default, players on mounts erode terrain 2.0 times faster than players on foot. (Needs Further Testing)
- *Eroded blocks that have not been walked on for a long time gradually revert to previous stages of erosion, simulating terrain recovery. The amount of time that has to pass before an unstepped block reverts to a previous erosion stage is controlled by a configurable de-erosion timeout window (measured in Minecraft days). (Needs Further Testing)

## Features

- **Spigot/Bukkit/Paper Support**
- **Customizable erosion speed based on number of steps (/setspeed)**
- **Multiplayer and GeyserMC Supported**

## Showcase
[![Watch the video](https://img.youtube.com/vi/OvsNVaFiK1Y/maxresdefault.jpg)](https://www.youtube.com/watch?v=OvsNVaFiK1Y)

<div align="center">
<img src="https://i.imgur.com/LOvPlVV.png" alt="map view" style="display: block; margin: 0 auto;">
</div>

<div align="center">
<img src="https://i.imgur.com/PHJaK3E.png" alt="map view" style="display: block; margin: 0 auto;">
</div>

<div align="center">
<img src="https://i.imgur.com/Uqg119h.png" alt="map view" style="display: block; margin: 0 auto;">
</div>

<div align="center">
<img src="https://i.imgur.com/zOpgx3F.png" alt="map view" style="display: block; margin: 0 auto;">
</div>

<br></br>
_<center>Traveller, there is no road. The road is made by walking.</center>_
<center>Antonio Machado</center>
<br></br>
