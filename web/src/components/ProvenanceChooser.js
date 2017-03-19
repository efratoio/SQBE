import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import JSONP              from 'jsonp';
// import Chart from '../d3utils/sample.js';
import {Card, CardActions, CardHeader, CardMedia, CardTitle, CardText} from 'material-ui/Card';
import FlatButton from 'material-ui/FlatButton';
// import * as N3 from 'n3'
import GraphComp from "./GraphCompModule";
import IconButton from 'material-ui/IconButton';
import {red500, yellow500, blue500,green500 ,grey500} from 'material-ui/styles/colors';
import ActionDone from 'material-ui/svg-icons/action/done';
import Dialog from 'material-ui/Dialog';
import RaisedButton from 'material-ui/RaisedButton';
import FontIcon from 'material-ui/FontIcon';
import $ from 'jquery';
import conf from "./constants"
const ajaxCallURL = `http://localhost:8080/Provenance?nodeList=`;
const sendProvData =  `http://localhost:8080/Provenance`;




export default class SingleProvenanceCard extends Component{
   static propTypes = {
    data: React.PropTypes.array.isRequired,
    sentData: React.PropTypes.func.isRequired,

  };
    constructor(props) {
    super(props);
    
    
    this.state = {
  
      inputValue : '',
      chosenNodes : [],
      color : green500,
      demi : false,
      open : false,

    }
    
  }

  componentDidMount() {
        this.update = true;
    }

  componentDidUpdate (){
  }
  componentWillUpdate (){

  }
  onSwitchState = (value) => {
    if(flag){
        this.setState({color : green500});
      }else{
        this.setState({color : grey500});
      }
    };
    onAddNode=(e)=>{
     
    };
    onAddEdge=(e)=>{

          var lst = this.state.chosenNodes;
          console.log("add",lst);
          lst = Array.concat(lst,e);
          console.log(lst)
          this.setState({chosenNodes: lst});
    };
    onGetData = () => {
        const data = {
        "example":this.props.data[2].num,
        "explanation":this.state.chosenNodes
      };
          this.props.sentData();

        if(!conf.debug){
        $.post(sendProvData,JSON.stringify(data)).done(function(){
        });
      }

    };
  
  onSetData = () => {
                this.setState({
                  demi:true

              });
       
    };
  handleOpen = () => {
    this.setState({open: true});
  };

  handleClose = () => {
    this.setState({open: false});
  };
    render() {
      console.log("render prov choos");
      const actions = [
            <FlatButton
              label="Submit"
              primary={true}
              keyboardFocused={true}
              onTouchTap={this.handleClose}
            />,
          ];

        return (
           <Card style={{width:500}}>
    <CardHeader
      title={this.props.data[2].name}
    />
   
      <CardMedia  >
      
        <div >
        <GraphComp  nodes={this.props.data[0]} 
        links={this.props.data[1]} 
        example = {this.props.data[2].num}
        onAddNode={this.onAddNode}
        onAddEdge={this.onAddEdge}
        demi={this.props.demi}
        /></div></CardMedia> 

   
    <CardText>
      Choose The nodes that explains the example
    </CardText>
    <CardActions>

    <RaisedButton style={{margin:6}} onClick={this.handleOpen}>
    Instructions
    </RaisedButton>
      <Dialog
          actions={actions}
          modal={false}
          open={this.state.open}
          onRequestClose={this.handleClose}
        >
          The nodes represent entities, while the edges represents relationships.
          Click the nodes that explains the example you chose.
          Then click on the edges that represents the right relationshops.
      </Dialog>

    <RaisedButton style={{margin:6}} onClick = {(e) => this.onGetData()} 
     icon={<ActionDone
      color={green500}/>}
      >
    </RaisedButton>

    </CardActions>
  </Card>
          
        );
    }

}
