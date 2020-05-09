<a><img src="https://github.com/shaniyu/DressCode/blob/master/Images/filterClothes.png" width="250" height="400" title="filterClothes"></a>
<a><img src="https://github.com/shaniyu/DressCode/blob/master/Images/homePage.png" width="250" height="400" title="homePage"></a>
<a><img src="https://github.com/shaniyu/DressCode/blob/master/Images/myClothes.png" width="250" height="400" title="myClothes"></a>
<a><img src="https://github.com/shaniyu/DressCode/blob/master/Images/mySets.png" width="250" height="400" title="mySets"></a>
<a><img src="https://github.com/shaniyu/DressCode/blob/master/Images/setsForCurrentWeather.png" width="250" height="400" title="setsForCurrentWeather"></a>
<a><img src="https://github.com/shaniyu/DressCode/blob/master/Images/sideMenu.png" width="250" height="400" title="sideMenu"></a>


<!-- [![FVCproductions](https://avatars1.githubusercontent.com/u/4284691?v=3&s=200)](http://fvcproductions.com) -->

<!--***INSERT GRAPHIC HERE (include hyperlink in image)***-->

# Magit web app

> the app functions similar to git, includes features such as commit, clone, pull, push, pull requests, merge, fetch,
branch managing, load and export repositories using XML. <br />
The web app was developed using JavaScript, HTML, CSS, JQuery and web services via Apache Tomcat.

<!-- > include terms/tags that can be searched -->

<!-- **Badges will go here**

- build status
- devDependencies
- npm package
- coverage
- slack
- downloads
- gitter chat
- license
- etc.

[![Build Status](http://img.shields.io/travis/badges/badgerbadgerbadger.svg?style=flat-square)](https://travis-ci.org/badges/badgerbadgerbadger) [![Dependency Status](http://img.shields.io/gemnasium/badges/badgerbadgerbadger.svg?style=flat-square)](https://gemnasium.com/badges/badgerbadgerbadger) [![Coverage Status](http://img.shields.io/coveralls/badges/badgerbadgerbadger.svg?style=flat-square)](https://coveralls.io/r/badges/badgerbadgerbadger) [![Code Climate](http://img.shields.io/codeclimate/github/badges/badgerbadgerbadger.svg?style=flat-square)](https://codeclimate.com/github/badges/badgerbadgerbadger) [![Github Issues](http://githubbadges.herokuapp.com/badges/badgerbadgerbadger/issues.svg?style=flat-square)](https://github.com/badges/badgerbadgerbadger/issues) [![Pending Pull-Requests](http://githubbadges.herokuapp.com/badges/badgerbadgerbadger/pulls.svg?style=flat-square)](https://github.com/badges/badgerbadgerbadger/pulls) [![Gem Version](http://img.shields.io/gem/v/badgerbadgerbadger.svg?style=flat-square)](https://rubygems.org/gems/badgerbadgerbadger) [![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org) [![Badges](http://img.shields.io/:badges-9/9-ff6799.svg?style=flat-square)](https://github.com/badges/badgerbadgerbadger)

- For more on these wonderful ~~badgers~~ badges, refer to <a href="http://badges.github.io/badgerbadgerbadger/" target="_blank">`badgerbadgerbadger`</a>. -->

<!-- ***INSERT ANOTHER GRAPHIC HERE***

[![INSERT YOUR GRAPHIC HERE](http://i.imgur.com/dt8AUb6.png)]()

- Most people will glance at your `README`, *maybe* star it, and leave
- Ergo, people should understand instantly what your project is about based on your repo

> Tips

- HAVE WHITE SPACE
- MAKE IT PRETTY
- GIFS ARE REALLY COOL

> GIF Tools

- Use <a href="http://recordit.co/" target="_blank">**Recordit**</a> to create quicks screencasts of your desktop and export them as `GIF`s.
- For terminal sessions, there's <a href="https://github.com/chjj/ttystudio" target="_blank">**ttystudio**</a> which also supports exporting `GIF`s.

**Recordit**

![Recordit GIF](http://g.recordit.co/iLN6A0vSD8.gif)

**ttystudio**

![ttystudio GIF](https://raw.githubusercontent.com/chjj/ttystudio/master/img/example.gif)

--- -->

<!-- ## Table of Contents

> If your `README` has a lot of info, section headers might be nice.

- [Installation](#installation)
- [Features](#features)
- [Contributing](#contributing)
- [Team](#team)
- [FAQ](#faq)
- [Support](#support)
- [License](#license) -->


---

<!-- ## Example (Optional)

```javascript
// code away!

let generateProject = project => {
  let code = [];
  for (let js = 0; js < project.length; js++) {
    code.push(js);
  }
};
```

--- -->

## Clone

- Clone this repo to your local machine using `https://github.com/shaniyu/MAGit-WebApp.git`

## Installation

- Clone the repository to your local machine

- Download tomcat from https://tomcat.apache.org/download-80.cgi. <br /> 
Pay attention that you are downloading tomcat 8.5.x ! <br />
Select the zip version suitable for your machine (windows\mac ...)

## Setup

- Unzip the tomcat zip file to any folder you like (e.g. c:\tomcat) <br />
Navigate to c:\tomcat\conf\tomcat-users.xml and open it <br />
Add the below lines (within the <tomcat-users> element):

``` XML
<role rolename="manager-gui"/>
<role rolename="admin-gui"/>
<user username="tomcat" password="tomcat" roles="manager-gui,admin-gui"/>
``` 

Save the file

- Drop the .WAR file in the root folder of the project inside C:\tomcats\webapps

- Start tomcat by invoking c:\tomcats\bin\startup.bat

- Open your browser (currently supported on chrome) and navigate to:
http://localhost:8080/magit

- To start using Magit by loading an existing repository (rather than creating an empty repository), you can use the example xml file [exampleRepository.xml](https://github.com/shaniyu/MAGit-WebApp/blob/master/exampleRepository.xml)

---

## Features

- commit, clone, pull, push, merge, fetch, branch managing (creation and deletion), load and export repositories using XML.

---

<!-- ## Contributing

> To get started...

### Step 1

- **Option 1**
    - 🍴 Fork this repo!

- **Option 2**
    - 👯 Clone this repo to your local machine using `https://github.com/joanaz/HireDot2.git`

### Step 2

- **HACK AWAY!** 🔨🔨🔨

### Step 3

- 🔃 Create a new pull request using <a href="https://github.com/joanaz/HireDot2/compare/" target="_blank">`https://github.com/joanaz/HireDot2/compare/`</a>.

--- -->

<!-- ## Team

> Or Contributors/People

| <a href="http://fvcproductions.com" target="_blank">**FVCproductions**</a> | <a href="http://fvcproductions.com" target="_blank">**FVCproductions**</a> | <a href="http://fvcproductions.com" target="_blank">**FVCproductions**</a> |
| :---: |:---:| :---:|
| [![FVCproductions](https://avatars1.githubusercontent.com/u/4284691?v=3&s=200)](http://fvcproductions.com)    | [![FVCproductions](https://avatars1.githubusercontent.com/u/4284691?v=3&s=200)](http://fvcproductions.com) | [![FVCproductions](https://avatars1.githubusercontent.com/u/4284691?v=3&s=200)](http://fvcproductions.com)  |
| <a href="http://github.com/fvcproductions" target="_blank">`github.com/fvcproductions`</a> | <a href="http://github.com/fvcproductions" target="_blank">`github.com/fvcproductions`</a> | <a href="http://github.com/fvcproductions" target="_blank">`github.com/fvcproductions`</a> |

- You can just grab their GitHub profile image URL
- You should probably resize their picture using `?s=200` at the end of the image URL.

---

## FAQ

- **How do I do *specifically* so and so?**
    - No problem! Just do this.

---

## Support

Reach out to me at one of the following places!

- Website at <a href="http://fvcproductions.com" target="_blank">`fvcproductions.com`</a>
- Twitter at <a href="http://twitter.com/fvcproductions" target="_blank">`@fvcproductions`</a>
- Insert more social links here.

---

## Donations (Optional)

- You could include a <a href="https://cdn.rawgit.com/gratipay/gratipay-badge/2.3.0/dist/gratipay.png" target="_blank">Gratipay</a> link as well.

[![Support via Gratipay](https://cdn.rawgit.com/gratipay/gratipay-badge/2.3.0/dist/gratipay.png)](https://gratipay.com/fvcproductions/)


---

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](http://opensource.org/licenses/mit-license.php)**
- Copyright 2015 © <a href="http://fvcproductions.com" target="_blank">FVCproductions</a>. -->