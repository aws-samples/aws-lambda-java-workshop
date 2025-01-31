#!/bin/bash

# Get API endpoint from AWS CLI using API name for REST API (v1)
API_NAME="Dragon Store API"

# Get API ID from name (REST API)
API_ID=$(aws apigateway get-rest-apis --region $AWS_REGION | \
    jq -r --arg NAME "$API_NAME" '.items[] | select(.name==$NAME) | .id')

if [ -z "$API_ID" ]; then
    echo "Failed to find REST API with name: $API_NAME"
    exit 1
fi

# Construct API endpoint for REST API
API_ENDPOINT="https://${API_ID}.execute-api.${AWS_REGION}.amazonaws.com/prod"

# Print API information for verification
echo "Found API ID: $API_ID"
echo "Using API endpoint: $API_ENDPOINT"

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
    fi
}

# Function to create dragon payload with random attributes
create_dragon_payload() {
    local id=$1
    local first_name=$(get_random_element "${FIRST_NAMES[@]}")
    local second_name=$(get_random_element "${SECOND_NAMES[@]}")
    local dragon_type=$(get_random_element "${TYPES[@]}")
    local color=$(get_random_element "${COLORS[@]}")
    local age=$(random_number 100 5000)
    
    echo "{
        \"id\": \"$id\",
        \"name\": \"$first_name$second_name\",
        \"type\": \"$dragon_type\",
        \"age\": \"$age\",
        \"color\": \"$color\"
    }"
}

# Function to run batch of requests with progress indicator
run_batch() {
    local operation=$1
    local total=$2
    local successful=0
    local failed=0
    
    echo "Starting $operation test..."
    for ((i=1; i<=$total; i++)); do
        echo -ne "Progress: $i/$total (Success: $successful, Failed: $failed)\r"
        
        case $operation in
            "CREATE")
                make_request "POST" "/dragons" "$(create_dragon_payload $i)"
                ;;
            "LIST")
                make_request "GET" "/dragons"
                ;;
            "GET")
                make_request "GET" "/dragons/$i"
                ;;
            "DELETE")
                make_request "DELETE" "/dragons/$i"
                ;;
        esac
        
        if [ $? -eq 0 ]; then
            ((successful++))
        else
            ((failed++))
        fi
        
        # Small delay to prevent overwhelming the API
        sleep 0.1
    done
    echo -e "\nCompleted $operation test (Success: $successful, Failed: $failed)"
}

# Main execution
echo "Starting load test at $(date)"
run_batch "CREATE" $NUM_REQUESTS
run_batch "LIST" $NUM_REQUESTS
run_batch "GET" $NUM_REQUESTS
run_batch "DELETE" $NUM_REQUESTS
echo "Load test completed at $(date)"