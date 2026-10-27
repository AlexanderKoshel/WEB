import React from "react"; 
import "./styles.css";

/**
 * Define Regions, a React component of lab1. The model
 * data for this view (the regions names) is available at
 * window.regionsModel.regionsModel().
 */
class Regions extends React.Component {
  constructor(props) {
    super(props);
    console.log(
      "window.lab1models.regionsModel()",
      window.lab1models.regionsModel()
    );
    this.state={substr : "",};

    
     this.handleChangeSubstr = (event) => this.handleChange(event);
  }
handleChange(event) {
 //  Insert your code here
    
  } 
  render() {
    return (
	<div>
           <div className="state-search">
            {			  
            this.state.substr
            }
           </div>
           <div className="lab1-example-output"><span id='IInfo'></span></div> 
          <label htmlFor="substrId">Enter substring to search:</label>
          <input
            id="substrId"
            type="text"
            value={this.state.substr}
            onChange={this.handleChangeSubstr}
          />
              
    </div>
    );
  }
}

export default Regions;
