#!/bin/bash

# Show usage information
show_help() {
    echo "Dragon Resort API Load Test Script"
    echo
    echo "Usage: $0 [OPTIONS] [api-endpoint]"
    echo
    echo "Options:"
    echo "  [--help] Shows this help message"
    echo
    echo "Arguments:"
    echo "  [api-endpoint] Optional. If not provided, will attempt to discover endpoint using AWS CLI"
    echo
    echo "Environment Variables:"
    echo "  [AWS_REGION] Required if no api-endpoint provided. AWS region for API discovery"
    echo
    echo "Examples:"
    echo "  $0                                                         # Uses AWS CLI for endpoint discovery"
    echo "  $0 https://api-id.execute-api.region.amazonaws.com/prod    # Uses provided endpoint"
    echo "  AWS_REGION=eu-central-1 $0                                 # Explicitly sets region for discovery"
}

# Show help if requested
if [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# Function to get API endpoint from AWS CLI
get_aws_endpoint() {
    # Check if AWS_REGION is set
    if [ -z "$AWS_REGION" ]; then
        echo "Error: AWS_REGION environment variable is not set"
        echo "Please set AWS_REGION or provide an API endpoint directly"
        echo
        show_help
        exit 1
    fi

    local api_name="Dragon Resort API"

    # Get API ID from name (REST API)
    local api_id=$(aws apigateway get-rest-apis --region $AWS_REGION | \
        jq -r --arg NAME "$api_name" '.items[] | select(.name==$NAME) | .id')

    if [ -z "$api_id" ]; then
        echo "Failed to find REST API with name: $api_name"
        exit 1
    fi

    # Construct API endpoint for REST API
    echo "https://${api_id}.execute-api.${AWS_REGION}.amazonaws.com/prod"
}


# Check if API endpoint is provided as argument
if [ $# -eq 1 ]; then
    API_ENDPOINT=$1
    echo "Using provided API endpoint: $API_ENDPOINT"
else
    # Get API endpoint from AWS CLI
    output=$(get_aws_endpoint 2>&1)
    exit_code=$?
    if [ $exit_code -ne 0 ]; then
        echo "$output"  # Print the error message
        exit $exit_code
    fi
    API_ENDPOINT=$output
    echo "Using AWS CLI discovered endpoint: $API_ENDPOINT"
fi



# Rest of the configuration
NUM_REQUESTS=100
CONTENT_TYPE="Content-Type: application/json"

# Dragon characteristics arrays
FIRST_NAMES=("Ancient" "Mighty" "Wise" "Fierce" "Noble" "Shadow" "Storm" "Crystal" "Ember" "Frost")
SECOND_NAMES=("Wing" "Claw" "Fang" "Scale" "Heart" "Soul" "Spirit" "Flame" "Thunder" "Ice")
TYPES=("Celestial Dragon" "Fire Dragon" "Ice Dragon" "Storm Dragon" "Earth Dragon")
COLORS=("bronze" "golden" "silver" "crimson" "azure" "emerald" "obsidian")

# Function to get random element from an array
get_random_element() {
    local array=("$@")
    local index=$((RANDOM % ${#array[@]}))
    echo "${array[$index]}"
}

# Function to generate random number in range
random_number() {
    local min=$1
    local max=$2
    echo $((RANDOM % (max - min + 1) + min))
}

# Function to create dragon payload with random attributes and occasional invalid JSON
create_dragon_payload() {
    local id=$1
    local first_name=$(get_random_element "${FIRST_NAMES[@]}")
    local second_name=$(get_random_element "${SECOND_NAMES[@]}")
    local dragon_type=$(get_random_element "${TYPES[@]}")
    local color=$(get_random_element "${COLORS[@]}")
    local age=$(random_number 100 5000)
    
    # Randomly decide to create an invalid payload (20% chance)
    if [ $((RANDOM % 5)) -eq 0 ]; then
        # Array of different ways to make invalid JSON
        local invalid_types=(
            "missing_comma"
            "missing_quote"
            "missing_field"
            "incomplete_json"
        )
        
        case $(get_random_element "${invalid_types[@]}") in
            "missing_comma")
                # Missing comma between fields
                echo "{
                    \"id\": \"$id\"
                    \"name\": \"$first_name$second_name\"
                    \"type\": \"$dragon_type\"
                    \"age\": \"$age\"
                    \"color\": \"$color\"
                }"
                ;;
            "missing_quote")
                # Missing closing quote
                echo "{
                    \"id\": \"$id\",
                    \"name\": \"$first_name$second_name,
                    \"type\": \"$dragon_type\",
                    \"age\": \"$age\",
                    \"color\": \"$color\"
                }"
                ;;
            "missing_field")
                # Randomly omit one field
                local fields=("id" "name" "type" "age" "color")
                local omit_field=$(get_random_element "${fields[@]}")
                local json="{"
                
                [ "$omit_field" != "id" ] && json="$json\"id\": \"$id\","
                [ "$omit_field" != "name" ] && json="$json\"name\": \"$first_name$second_name\","
                [ "$omit_field" != "type" ] && json="$json\"type\": \"$dragon_type\","
                [ "$omit_field" != "age" ] && json="$json\"age\": \"$age\","
                [ "$omit_field" != "color" ] && json="$json\"color\": \"$color\""
                
                echo "$json}"
                ;;
            "incomplete_json")
                # Incomplete JSON structure
                echo "{
                    \"id\": \"$id\",
                    \"name\": \"$first_name$second_name\",
                    \"type\": \"$dragon_type\","
                ;;
        esac
        
        echo "Generated invalid payload for testing error handling" >&2
    else
        # Generate valid payload
        echo "{
            \"id\": \"$id\",
            \"name\": \"$first_name$second_name\",
            \"type\": \"$dragon_type\",
            \"age\": \"$age\",
            \"color\": \"$color\"
        }"
    fi
}

# Function to make API calls with error handling and response logging
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    response=$(curl -s -w "\n%{http_code}" -X "$method" \
        "$API_ENDPOINT$endpoint" \
        -H "$CONTENT_TYPE" \
        ${data:+-d "$data"})
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | sed '$d')
    
    if [ "$status_code" -lt 200 ] || [ "$status_code" -gt 299 ]; then
        echo "Error: $method request to $endpoint failed with status $status_code"
        echo "Response: $response_body"
        # Check if response contains invalid JSON error messages
        if echo "$response_body" | grep -q "Unexpected end-of-input\|Illegal unquoted character\|Unexpected character"; then
            return 2  # Invalid JSON payload
        fi
        return 1  # Regular error
    fi
    return 0  # Success
}

# Function to run batch of requests with progress indicator and invalid payload handling
run_batch() {
    local operation=$1
    local total=$2
    local successful=0
    local failed=0
    local invalid_payloads=0
    
    echo "Starting $operation test..."
    for ((i=1; i<=$total; i++)); do
        echo -ne "Progress: $i/$total (Success: $successful, Failed: $failed, Invalid: $invalid_payloads)\r"
        
        case $operation in
            "CREATE")
                payload=$(create_dragon_payload $i)
                make_request "POST" "/dragons" "$payload"
                result=$?
                
                case $result in
                    0) ((successful++));;
                    2) ((invalid_payloads++));;
                    *) ((failed++));;
                esac
                ;;
            "LIST")
                if make_request "GET" "/dragons"; then
                    ((successful++))
                else
                    ((failed++))
                fi
                ;;
            "GET")
                if make_request "GET" "/dragons/$i"; then
                    ((successful++))
                else
                    ((failed++))
                fi
                ;;
            "DELETE")
                if make_request "DELETE" "/dragons/$i"; then
                    ((successful++))
                else
                    ((failed++))
                fi
                ;;
        esac
        
        # Small delay to prevent overwhelming the API
        sleep 0.1
    done
    echo -e "\nCompleted $operation test (Success: $successful, Failed: $failed, Invalid: $invalid_payloads)"
}


# Main execution
echo "Starting load test at $(date)"
run_batch "CREATE" $NUM_REQUESTS
run_batch "LIST" $NUM_REQUESTS
run_batch "GET" $NUM_REQUESTS
run_batch "DELETE" $NUM_REQUESTS
echo "Load test completed at $(date)"