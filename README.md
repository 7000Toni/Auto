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

## Demo Videos
<video src="https://github.com/user-attachments/assets/b3edd69a-b36f-49db-9bc1-712eaa06a0b7" controls width="100%"></video>
<video src="https://github.com/user-attachments/assets/16912283-ff37-4a7f-a426-0a461c8e4431" controls width="100%"></video>
<video src="https://github.com/user-attachments/assets/cf21ef4e-eea1-40ca-813c-ef7eb118d36e" controls width="100%"></video>

## Screenshots
![App Screenshot](./assets/load1.png)
![App Screenshot](./assets/load2.png)
![App Screenshot](./assets/load3.png)
![App Screenshot](./assets/load4.png)
![App Screenshot](./assets/chart1.png)
![App Screenshot](./assets/chart2.png)
![App Screenshot](./assets/chart3.png)
![App Screenshot](./assets/chart4.png)
![App Screenshot](./assets/menu1.png)
![App Screenshot](./assets/menu2.png)
![App Screenshot](./assets/menu3.png)
![App Screenshot](./assets/timeframes1.png)
![App Screenshot](./assets/timeframes2.png)
![App Screenshot](./assets/timeframes3.png)
![App Screenshot](./assets/colours1.png)
![App Screenshot](./assets/colours2.png)
![App Screenshot](./assets/colours3.png)
![App Screenshot](./assets/colours4.png)
![App Screenshot](./assets/colours5.png)
![App Screenshot](./assets/images1.png)
![App Screenshot](./assets/images2.png)
![App Screenshot](./assets/images3.png)
![App Screenshot](./assets/replay1.png)
![App Screenshot](./assets/replay2.png)
![App Screenshot](./assets/replay3.png)
![App Screenshot](./assets/replay4.png)
![App Screenshot](./assets/replay5.png)
![App Screenshot](./assets/replay6.png)
![App Screenshot](./assets/replay7.png)
![App Screenshot](./assets/replay8.png)
![App Screenshot](./assets/replay9.png)
![App Screenshot](./assets/replay10.png)
![App Screenshot](./assets/replay11.png)
![App Screenshot](./assets/multiplecharts1.png)
![App Screenshot](./assets/multiplecharts2.png)

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

<!--## Project Architecture-->

