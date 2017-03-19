import React from 'react';
import Paper from 'material-ui/Paper';

const sendProvData =  `http://localhost:8080/Query`;

const styles={
Paper: {

  margin: 20,
  textAlign: 'center',
  display: 'inline-block',
  }
};
const QueryPaper = React.createClass({
    propTypes: {
        
        demi: React.PropTypes.bool
      },
       getInitialState () {
    return {
    	text: ""
    }
      
    
  },
   getDefaultProps: function() {
    return {
      demi: false
    };
  },
  componentWillReceiveProps(nextProps){
    console.log("Getting top 1 query");
    if(nextProps.demi){
  		const xhr = new XMLHttpRequest();
	    URL = sendProvData;
	    xhr.open('GET', URL);
	    xhr.addEventListener('load', () => {
	     const results = JSON.parse(xhr.responseText); 
        console.log("Query",xhr.responseText);
          this.setState({text: results.query,});
      });
	      
  	 xhr.send();
    }
  },
  render: function() {
    console.log("render query present");
    return <Paper style={styles.Paper} zDepth={5} rounded={false}
    children={<div>
    {this.state.text}
    </div>}/>
    
    
; 
  }

  });
  export default QueryPaper;
