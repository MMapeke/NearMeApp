# NearMe

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
NearMe allows users to view short descriptions of events near them along with short media from users at the events. Can be viewed on a list or map showing whats near you. Users can also post information about events currently happening, with the location, short description, and short video.

### App Evaluation

- **Category:** Social/Video
- **Mobile:** View Only, uses camera, intended for mobile only experience
- **Story:** Allows users to find events near them and view short info and media of whats currently going on.
- **Market:** Anyone looking for something to do or to go to near them could find use for this app. Can also share what's near you for your profile or to get more people to come. 
- **Habit:** Users can very easily check what's going on near them with a visual guide, and short stories make consumption easy. Habit forming because users will be interested/curious on whats happening around them and intuitive viewing experience.
- **Scope:** Start out only focused on realtime events going on nearby. Can expand to recommendations, liking content.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

- [x] User can login/register/logout
- [x] User can use app with current location/search a location
- [x] User can view posts through Map and Feed view.
- [x] User can view more details about posts from both screens
- [x] Map and Feed View query same posts in intuitive way
- [x] User can post short description and media for an event at a location

**Optional Nice-to-have Stories**

- [x] Posts are recommended to user based off last location
- [x] User can view and interact with other profiles
- [x] User can customize own profile, view own posts, and delete posts 
- [x] User can like posts
- [x] User can switch viewing modes (ViewAll/Manual Defa) 
- [x] Items in map view are clustered based off locaton, and can be viewed/interacted with
- [x] Infinite Pagination and Refresh on Feed view
- [x] General UI Improvements, Color Scheme, Animations
- [ ] Location/Zoom Persistence
- [ ] Video Support
- [ ] More User to User Support: Following, View Following, etc.

### 2. Screen Archetypes

* Login Screen/ Registration Screen
   * User can login/create new acct
* Choose Location
    * User can choose btwn input location, or use curr location
* Profile
    * User can view and edit profile
* List View
    * User can view list of posts
* Map View
   * User can view map of posts
* Compose Post
    * User can post short info for local event w/ short desc, location, and media
* More Details
   * User can view video and description


### 3. Navigation

**Tab Navigation** (Tab to Screen)

*  Map View
*  List View
*  Compose Post
*  Profile
*  Choose Location
*  More Details/Recommendation Details

**Flow Navigation** (Screen to Screen)

* Login Screen/Registration
  => Location Screen
* Profile Screen
  => Main Screen w/ Tab Navigation
  => Login Screen/Registraton
  => More Details/Recommendation 
* Location Screen
  => Main Screen w/ Tab Navigation
* Compose Post
  => Main Screen w/ Tab Navigation
  => Login Screen/Registraton
  => Recommendation Details
* Map/List View
  => Main Screen w/ Tab Navigation
  => Login Screen/Registraton
  => More Details/Recommendation 
* More Details
  => Main Screen w/ Tab Navigation

## Wireframes
<img src="nearMeWireframe.jpg" width=600>


## Schema 
### Models
#### Post

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | user          | Pointer to User| post creator |
   | media         | File     | video that user posts |
   | desc          | String   | description user made |
   | location      | GeoPoint   |where user made post for |
   | createdAt     | DateTime | date when post is created (default field) |
   | likedBy       | Array   | collection of userID's that have liked post|
   
 #### User

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | username      | String   | username |
   | password      | String   | password |
   | last_location| GeoPoint   | last updated latitude |
   | createdAt     | DateTime | date when post is created (default field) |
   | profilePic    | File    | profile pic file



### Networking
**List of network requests by screen**
- Login/Register Screen
    - (Create/POST) Create a new user object
    - (Read/GET) Query logged in user object
- Where You At Screen
    - (Update/PUT) Update Logged in User object w/ location
- Compose Post Screen
    - (Create/POST) Create a new post object associated w/ User
- Map/List View
    - (Read/GET) Query all posts, sorted based on time creatd
    - (Update/PUT) Liking posts, updates the post
- More Details View
    - (Update/PUT) Liking posts, updates the post
- Profile Screen
    - (Read/GET) Query logged in user object
    - (Delete) Delete previous posts

