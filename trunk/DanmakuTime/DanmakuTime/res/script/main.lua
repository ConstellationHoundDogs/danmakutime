
CircleGhost = {xmul=100, ymul=100}

function CircleGhost.new(o)
	local o = extend(CircleGhost, o or {})
	return Sprite.new(o)
end

function CircleGhost:init()
	print("CircleGhost:init")
	
	self:setPos(400, 300);
	self:setTexture(textureStore:getTexture("test.png#g1"));
end

function CircleGhost:update()
	print("CircleGhost:update")
	
	local n = 0
	while n < 1000 do	
		self:setPos(400 + self.xmul * math.cos(n), 300 + self.ymul * math.sin(n))
		n = n + 1
		
		yield()
	end
	
end

--------------------------------------------------------------------------------

function main()
	local ghost = CircleGhost.new{xmul=100, ymul=100}
	ghost:test("abc", 1)
    yield(10)
	ghost:test("def", 1)

	local ghost2 = CircleGhost.new{xmul=-100, ymul=100}
	
	Thread.new(function()
		while true do
			if input:consumeKey(Keys.Z) then
				print("pew pew")
			end
			yield()
		end
	end)
	
	for group=1,50 do
		for n=1,200 do
			local s = Sprite.new()
			
			if math.random(10) >= 10 then
				s:setTexture(textureStore:getTexture("test.png#g0"));
				s:setZ(-1)
				s:setDrawAngleAuto(false)
			else
				s:setTexture(textureStore:getTexture("test.png#g1"));
			end
			
			s:setPos(600, 300)
			s:setSpeed(2 + math.random() * 2)
			s:setAngle(256)
			s:setAngleInc(1 + math.random() * 1)
		end		
		yield(10)
	end		
end
