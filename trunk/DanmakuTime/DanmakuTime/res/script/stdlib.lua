
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
