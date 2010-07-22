
function clone(obj)
	local result = {}
	for k,v in pairs(obj) do
		result[k] = v
	end
	return result
end

function extend(a, b)
	local result = {}
	for k,v in pairs(a) do
		result[k] = v
	end
	for k,v in pairs(b) do
		result[k] = v
	end
	return result
end

function signum(x)
	if x > 0 then
		return 1
	elseif x < 0 then
		return -1
	end
	return 0
end

function pauseHandler()
	local dx = screenWidth/2
	local dy = screenHeight/2

	local pausedText = TextDrawable.new(999)
	pausedText:setPos(dx, dy)
	pausedText:setBlockAnchor(5)
	pausedText:setFontName("DejaVuSans")
	pausedText:setFontStyle(FontStyle.BOLD)
	pausedText:setFontSize(14)	
	pausedText:setText("Paused")
	pausedText:setZ(-100)

	while true do
		if input:consumeKey(Keys.ESCAPE) then
			break
		end
		
		yield()
	end
	
	pausedText:destroy()
end
