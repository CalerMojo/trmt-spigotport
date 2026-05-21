<p align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/177c11b808636328e9360ff476341a49e7af3167.png" alt="TRMT cover">
</p>

# The Roads More Travelled (Plugin Port)

A dynamic terrain plugin based on the Fabric mod The Roads More Traveled! This plugin adds a server-side gradual erosion system to the game, slowly transforming the routes you travel more often through into beautiful and realistic looking paths. Make your world feel more immersive by etching the story of your explorations onto the landscape.

## Logic
- Grass blocks, dirt blocks, and sand blocks accumulate an erosion index everytime they're are stepped on.
- Upon reaching a threshold, each one of these blocks transforms into their eroded variant progressing through different stages of erosion until reaching a final stage. *Leaves blocks and vegeation are trampled when reaching their threshold. (Will be added in v1.0.3
- Erosion thresholds are randomly determined, making erosion happen in a more organic, less determnistic way.
- *Erosion can be triggered by players, players on mounts or mobs on leashes. The strength of the erosion is controlled by a configurable multiplier. By default, players on mounts erode terrain 2.0 times faster than players on foot. (Will be added in v1.0.3)
- *Eroded blocks that have not been walked on for a long time freeze in its current state to increase server performance. The amount of time that has to pass before an unstepped block freezes is controlled by the "/trmt setdays" command (measured in Minecraft days). (Will be added in v1.0.3)

## Current Features

- **Spigot/Bukkit/Paper Support**
- **Customizable erosion speed based on number of steps (/setspeed)**
- **Multiplayer and GeyserMC Supported**

## Upcoming Features

- **Customizable de-erosion mechanic that will freeze blocks in it's current state to increase server performance**
- **A trample mechanic for leaves and vegetation**
- **Riding on mounts, mobs on leashes, wearing leather boots, and feather falling will increase/decrease the rate of errosion at a configurable rate**
- **Porting the plugin as a complete vanilla server side mod to work on Fabric/Forge without the need for client install**

<br></br>
_<center>Traveller, there is no road. The road is made by walking.</center>_
<center>Antonio Machado</center>
<br></br>
