
function clone(obj)
	local result = {}
	for k,v in pairs(obj) do
		result[k] = v
	end
	return result
end

--Takes a list of tables and generates a new table containing SHALLOW copies
--of all attributes
function extend(...)
	local result = {}
	for tableIndex,table in ipairs(arg) do
		for k,v in pairs(table) do
			result[k] = v
		end
	end
	return result
end

function append(a, b)
	for i,v in ipairs(b) do
		table.insert(a, v)
	end
end

function signum(x)
	if x > 0 then
		return 1
	elseif x < 0 then
		return -1
	end
	return 0
end

function waitForDeath(...)
    local t = {}
    for _,v in ipairs(arg) do
        if type(v) == "table" then
            append(t, v)
        elseif v ~= nil then
            table.insert(t, v)
        end
    end

    while true do        
        local stillAlive = false
        for _,v in ipairs(t) do
            if not v:isDestroyed() then
                stillAlive = true
                break
            end
        end
        if not stillAlive then
            break
        end
        yield()
    end
end

function getClosestPlayer(x, y)
    local best = nil
    local bestDistSq = 9999999
    for _,p in ipairs(players) do
        if p ~= nil and not p:isDestroyed() then
            local dx = p:getX() - x
            local dy = p:getY() - y
            local distSq = (dx*dx) + (dy*dy)
            if best == nil or distSq < bestDistSq then
                best = p
                bestDistSq = distSq
            end
        end    
    end
    return best
end
