# Auto

Auto is a Java desktop application I built to allow me to view futures data in the form of tick charts without requiring a third party application (They are not affordable for me at the moment). The name originally came from what I thought I would end up using this application for, which is developing automated trading algorithms and visualizing what the algorithm is doing. 

It has evolved from a prototype into a tool that I use every day. The custom UI framework that's used started as a solution to the very first problem I encountered, which was drawing the entire chart onto a JavaFX canvas. Because drawing the entire chart at once exceeded the texture size limit for the canvas, I was forced to generate a portion of it depending on where the scroll bar was. I didn't like the way the default scroll bar looked so I decided to try making my own custom scroll bar. Once I had successfully implemented my own scroll bar, I felt more comfortable creating my own UI elements and I thought it would be a fun exercise as well. 

This initial step led to me adding all sorts of new features, some for fun and some because they were needed. As the project grew, it became an opportunity to explore custom UI development, modular architecture, and long-term software maintenance.


## Technologies Used

Java,
JavaFX
## Main Features

Data visualizing as tick chart or candlestick chart
- Up to 6 different data sources can be loaded at once
- Multiple charts from any data source can be opened simultaneously
- Synchronized crosshair across different charts of the same data source
- Custom timeframes


Market simulation
- Trading functionality similar to tradingview
- Trade history can be saved to a file and viewed on a chart
- Pausing, seeking and speed adjustments are possible
- Detailed report of each trade can be saved to a file

Customization
- Dark mode and light mode each with separate settings
- Chart background colours, line chart colours and candlestick colours are customizable
- Can use an image as a background on a chart

## Lessons Learned

-  It's important to think about the algorithm you're using when solving a problem rather than using the easiest or most intuitive algorithm. The most intuitive algorithm might be one that works well for humans but doesn't work as well for computers

-  I enjoy coding so much that I often start working on something without thinking about the full design or solution which can often lead to design flaws which need to be corrected in the future. Being efficient usually means making a full design first then implementing it

- It's useful to consider future additions that will be made to the project so that you can create a design thats easy to extend and maintain in the future

- Things NEVER take as long as you think. Take your estimate for how long something will take, double it, and you'll be lucky to finish within 3x your original estimate
## Future Improvements

- Add drawings
- Save all program settings not just visual settings
- Improve resource usage
- Refactor some code
## Project Architecture


## Screenshots
