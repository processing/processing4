const _ = require('lodash');

// Utility functions that can be called from Java
const ProcessingUtils = {
    // Array manipulation using lodash
    shuffleArray: (array) => {
        return _.shuffle(array);
    },

    // Statistical functions
    getArrayStats: (numbers) => {
        return {
            mean: _.mean(numbers),
            sum: _.sum(numbers),
            min: _.min(numbers),
            max: _.max(numbers),
            size: numbers.length
        };
    },

    // String manipulation
    capitalizeWords: (text) => {
        return _.startCase(_.toLower(text));
    },

    // Object utilities
    deepClone: (obj) => {
        return _.cloneDeep(obj);
    },

    // Generate random colors for Processing sketches
    generateColorPalette: (count) => {
        const colors = [];
        for (let i = 0; i < count; i++) {
            colors.push({
                r: _.random(0, 255),
                g: _.random(0, 255),
                b: _.random(0, 255)
            });
        }
        return colors;
    }
};

// Export for Node.js usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ProcessingUtils;
}

// Make available globally for other contexts
if (typeof global !== 'undefined') {
    global.ProcessingUtils = ProcessingUtils;
}

// Function to handle command line arguments and return JSON
function handleCommand(command, data) {
    try {
        const parsedData = JSON.parse(data);
        let result;

        switch(command) {
            case 'shuffle':
                result = ProcessingUtils.shuffleArray(parsedData);
                break;
            case 'stats':
                result = ProcessingUtils.getArrayStats(parsedData);
                break;
            case 'capitalize':
                result = ProcessingUtils.capitalizeWords(parsedData);
                break;
            case 'colors':
                result = ProcessingUtils.generateColorPalette(parsedData);
                break;
            default:
                result = { error: 'Unknown command: ' + command };
        }

        console.log(JSON.stringify(result));
    } catch (error) {
        console.log(JSON.stringify({ error: error.message }));
    }
}

// Handle command line execution
if (require.main === module) {
    const args = process.argv.slice(2);
    if (args.length >= 2) {
        handleCommand(args[0], args[1]);
    } else {
        console.log(JSON.stringify({ error: 'Usage: node index.js <command> <data>' }));
    }
}