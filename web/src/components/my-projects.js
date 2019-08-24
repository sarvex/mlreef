import React from "react";
import Navbar from "./navbar/navbar";
import ProjectSet from "./projectSet";
import "../css/project-overview.css";

class Myprojects extends React.Component {
  state = {
    showOverview: true,
    showProjects: false,
    showRepo: false
  }

  handleOverview = () => {
    this.setState({ showOverview: true, showProjects: false, showRepo: false })
  }

  handleProjectList = () => {
    this.setState({ showOverview: false, showProjects: true, showRepo: false })
  }

  handleRepo = () => {
    this.setState({ showOverview: false, showProjects: false, showRepo: true })
  }

  render() {
    return (
      <div>
        <Navbar />
        <div className="project-content">
          <NewProject />
          <hr />
          <ProjectSet />
        </div>
      </div>
    )
  }
}

const NewProject = () => {
  return (
    <div className="new-project">
      <p id="title">Projects</p>
      <button className="add">
        New Project
      </button>
    </div>
  )
}

export default Myprojects;
