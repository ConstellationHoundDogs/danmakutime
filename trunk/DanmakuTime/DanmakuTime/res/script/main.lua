
Invulnerable = {
	xmul=100,
	ymul=100
	}

function Invulnerable.new(obj)
	obj = extend(THSprite, Invulnerable, obj or {})
	return THSprite.new(obj)
end

function Invulnerable:init()
	self:setPos(400, 300);
	self:setZ(-100)
	self:setTexture(texStore:get("test.png#g1"));
	self:setColNode(0, enemyColType, CircleColNode.new(7))
end

function Invulnerable:update()
	local n = 0
	while n < 100 do	
		self:setPos(levelWidth/2 + self.xmul * math.cos(n),
			levelHeight/2 + self.ymul * math.sin(n))
		n = n + 1
		
		yield()
	end
	
	self:destroy()
end

function Invulnerable:animate()
	while false do
		self:setTexture(texStore:get("test.png#g0"));
		yield(10)
		self:setTexture(texStore:get("test.png#g1"));
		yield(10)
	end
end

function Invulnerable:onDestroy()
	--return false from this function to prevent the destruction of the object
	--You can call destroy() again later to attempt another destruction
	return false
end

--------------------------------------------------------------------------------

function main()
	--soundEngine:setBGM("bgm/bgm01.ogg");

	buildLevel("level-bg.png")

	local ghost = Invulnerable.new{xmul=100, ymul=100}	
    yield(10)
	local ghost2 = Invulnerable.new{xmul=-100, ymul=100}
	
	Thread.new(function()
		while true do
			if input:consumeKey(Keys.Q) then
				print("The global key listener thread saw your key. And will proceed to kill you.")
				player:destroy()
			end
			yield()
		end
	end)
	
	Thread.new(fireLaser)
	
	for group=1,50 do
		for n=1,200 do
			local s = THSprite.new{hp=1, power=1}
			s:setColNode(0, enemyColType, CircleColNode.new(7))
			s.onCollision = function(self, other, myNode, otherNode)
				self.hp = self.hp - other.power
				if self.hp <= 0 then
					self:destroy()
				end
			end
			
			if math.random(10) >= 10 then
				s:setTexture(texStore:get("test.png#g0"));
				s:setZ(-1)
				s:setDrawAngleAuto(false)
			else
				s:setTexture(texStore:get("test.png#g1"));
				s:setBlendMode(BlendMode.ADD)
			end
			
			s:setPos(levelWidth*.9, levelHeight*.35)
			s:setSpeed(2 + math.random() * 2)
			s:setAngle(256)
			s:setAngleInc(2 + math.random() * 1)
			
			--soundEngine:playSound("sfx01.ogg")
		end		
		yield(10)
	end
end

function fireLaser()
	while true do
		local r = 8
	
		local s = THSprite.new{hp=1, power=1}
		--s:setColNode(0, enemyShotColType, LineSegColNode.new(0, -8+r, 0, 8-r, r))
		--lineseg will have length==0, which is the same as this circle col node:
		s:setColNode(0, enemyShotColType, CircleColNode.new(r))
		
		s.onCollision = function(self, other, myNode, otherNode)
			self:destroy()
		end
		s:setTexture(texStore:get("test.png#laser"));
		s:setBlendMode(BlendMode.ADD)
		
		s:setPos(levelWidth*.9, -32)
		s:setSpeed(2)
		s:setAngle(256)
		s:setAngleInc(.5)
		
		yield(2)
	end
end
