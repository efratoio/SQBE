import * as d3 from 'd3'

export default class D3GraphObj {

    constructor(el, props, nodes, links) {
        this.el = el;
        this.props = props;
        this.nodes = nodes;
        this.links = links;
    }

    /**
     * To override. Creates the initial rendering of the chart.
     */
    create() {
        this.createRoot();
    }

    /**
     * Creates the root-level SVG element.
     * @return {object} D3 SVG root.
     */
    createRoot() {
        const {width, height, margin} = this.props;

        const svg = d3.select(this.el);
        var w = 1000;
        var h = 600;
        var linkDistance=200;

        var colors = d3.scaleLinear()
                
        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function(d) { return d.index }))
            .force("collide",d3.forceCollide( function(d){return d.r + 8 }).iterations(16) )
            .force("charge", d3.forceManyBody())
            .force("y", d3.forceY(0))
            .force("x", d3.forceX(0));
    
        var link = svg.append("g")
            .attr("class", "links")
            .selectAll("line")
            .data(this.links);
            
        
        var node = svg.append("g")
            .attr("class", "nodes")
            .selectAll("circle")
            .data(this.nodes);
                
        
        
        var ticked = function() {
            link
                .attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });
    
            node
                .attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; });
        }  
        
      
        
        console.log(svg);
        return svg;
    }
    /**
     * To override. Populates the initial renderings with content.
     */
    update() {
       
    }

  

    /**
     * Can be overriden. Destroys the rendered SVG.
     */
    destroy() {
        d3.select(this.el).selectAll('svg').remove();
    }
}


 