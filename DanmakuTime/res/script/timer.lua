
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- 
-- External dependencies: none
--
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

Timer = {
	time=0,
	onTick=nil,
	onFinished=nil,
	framesPerTick=60
	}

function Timer.new(time, onFinished, onTick, framesPerTick)
	local self = extend(Timer, {time=time, onTick=onTick, onFinished=onFinished})
	if framesPerTick ~= nil then
		self.framesPerTick = framesPerTick
	end	
	
	local thread = Thread.new(function()
		for n=1,self.time do
			yield(self.framesPerTick)
			if self.onTick ~= nil then
				self.onTick()
			end
		end
		if self.onFinished ~= nil then
			self.onFinished()
		end
	end)
	
	return self
end

function Timer:destroy()
	self.time = 0
	self.onTick = nil
    self.onFinished = nil
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
